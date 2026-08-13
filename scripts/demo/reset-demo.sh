#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=scripts/demo/lib.sh
source "${SCRIPT_DIR}/lib.sh"

demo_init
DEMO_ENV_LOADED=1
export DEMO_ENV_LOADED
demo_acquire_lock
trap demo_release_lock EXIT

candidate_generation="00000000-0000-4000-8000-000000000000"
generated_candidate=""
preflight_secret=""
preflight_db_password=""
next_reset_at=""
work_dir=""
mapping_file=""
failure_stage="bootstrap"
pre_prune_completed="false"
candidate_db_present="false"
candidate_files_present="false"
live_backend_started="false"
reset_started_at=""
readiness_timeout_seconds="${DEMO_SMOKE_TIMEOUT_SECONDS:-120}"
readiness_attempts=""

write_lifecycle_state() {
  local state="$1"
  local filename="${2:-status.json}"
  demo_tool write-state \
    --seed-dir /seed \
    --state-dir /state \
    --filename "${filename}" \
    --state "${state}" \
    --candidate "${candidate_generation}" \
    --next-reset-at "${next_reset_at}" \
    --expected-app-version "${DEMO_COMPATIBLE_APP_VERSION}"
}

write_resetting_lifecycle_state() {
  demo_tool write-resetting-state \
    --state-dir /state \
    --candidate "${candidate_generation}" \
    --next-reset-at "${next_reset_at}"
}

write_failed_lifecycle_state() {
  demo_tool write-failed-state \
    --state-dir /state \
    --candidate "${candidate_generation}" \
    --next-reset-at "${next_reset_at}"
}

capture_container_log() {
  local container="$1"
  local destination="$2"
  if docker container inspect "${container}" >/dev/null 2>&1; then
    docker logs "${container}" > "${destination}" 2>&1 || true
  fi
}

write_control_plane_failure_log() {
  local exit_code="$1"
  local line="$2"
  local failed_state_published="$3"
  docker run --rm --pull never --network none --read-only --log-driver none \
    --cap-drop ALL --security-opt no-new-privileges:true \
    -e PYTHONDONTWRITEBYTECODE=1 \
    -v "${DEMO_PROJECT_ROOT}/scripts/demo/demo_control.py:/opt/demo-control.py:ro" \
    -v "${DEMO_LOG_DIR}:/logs" \
    --entrypoint python \
    "${DEMO_CONTROL_IMAGE}" \
    /opt/demo-control.py write-control-plane-failure-log \
    --logs-root /logs \
    --candidate "${candidate_generation}" \
    --stage "${failure_stage}" \
    --line "${line}" \
    --exit-code "${exit_code}" \
    --failed-state-published "${failed_state_published}"
}

wait_for_preflight_readiness() {
  local attempt running
  for attempt in $(seq 1 "${readiness_attempts}"); do
    running="$(docker inspect --format '{{.State.Running}}' "${DEMO_PREFLIGHT_CONTAINER}" 2>/dev/null || true)"
    if [[ "${running}" != "true" ]]; then
      capture_container_log "${DEMO_PREFLIGHT_CONTAINER}" \
        "${DEMO_LOG_DIR}/preflight-${candidate_generation}.log"
      demo_fail "candidate backend exited before readiness check passed"
    fi
    if docker exec "${DEMO_PREFLIGHT_CONTAINER}" \
      curl -fsS http://127.0.0.1:8080/actuator/health/readiness >/dev/null 2>&1; then
      return
    fi
    if [[ "${attempt}" == "${readiness_attempts}" ]]; then
      demo_fail "candidate backend readiness timeout"
    fi
    sleep 2
  done
}

on_failure() {
  local exit_code="$1"
  local line="$2"
  if ((BASH_SUBSHELL > 0)); then
    exit "${exit_code}"
  fi
  trap - ERR INT TERM
  set +e
  demo_log "ERROR: reset failed: stage=${failure_stage} line=${line} exit=${exit_code} candidate=${candidate_generation}"
  local failed_state_published="false"
  if write_failed_lifecycle_state; then
    failed_state_published="true"
  fi
  if [[ "${pre_prune_completed}" == "true" ]]; then
    capture_container_log "${DEMO_PREFLIGHT_CONTAINER}" \
      "${DEMO_LOG_DIR}/preflight-${candidate_generation}.log"
    if [[ "${live_backend_started}" == "true" ]]; then
      demo_compose logs --no-color --tail=500 backend \
        > "${DEMO_LOG_DIR}/backend-${candidate_generation}.log" 2>&1 || true
    fi
  elif ! write_control_plane_failure_log "${exit_code}" "${line}" "${failed_state_published}"; then
    demo_log "ERROR: bounded control-plane failure log publication failed"
  fi
  if docker container inspect "${DEMO_PREFLIGHT_CONTAINER}" >/dev/null 2>&1; then
    docker rm -f "${DEMO_PREFLIGHT_CONTAINER}" >/dev/null 2>&1 || true
  fi
  demo_db_root -e "DROP USER IF EXISTS '${DEMO_PREFLIGHT_DB_USER}'@'%';" \
    >/dev/null 2>&1 || true
  demo_compose stop -t 10 backend >/dev/null 2>&1 || true
  if [[ "${pre_prune_completed}" != "true" ]]; then
    demo_log "ERROR: pre-prune gate was not completed; generation logs suppressed"
  elif [[ "${failed_state_published}" == "true" ]]; then
    demo_log "ERROR: FAILED state published; available artifacts retained: candidateDb=${candidate_db_present} candidateFiles=${candidate_files_present} liveBackendStarted=${live_backend_started}"
  else
    demo_log "ERROR: FAILED state publication failed; backend stopped; available artifacts retained: candidateDb=${candidate_db_present} candidateFiles=${candidate_files_present} liveBackendStarted=${live_backend_started}"
  fi
  exit "${exit_code}"
}
trap 'on_failure $? $LINENO' ERR
trap 'on_failure 130 $LINENO' INT TERM

failure_stage="candidate-generation"
[[ -r /proc/sys/kernel/random/uuid ]] \
  || demo_fail "kernel UUID source is unavailable"
IFS= read -r generated_candidate < /proc/sys/kernel/random/uuid
[[ "${generated_candidate}" =~ ^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ ]] \
  || demo_fail "candidate UUID generation failed"
candidate_generation="${generated_candidate}"

failure_stage="reset-schedule"
next_reset_at="$(demo_next_reset_at)"

# The state publication is the write barrier. Keep every optional or potentially
# slow inspection after it so no visitor write can enter the retiring generation.
failure_stage="resetting-state"
write_resetting_lifecycle_state

failure_stage="backend-stop"
demo_compose stop -t 30 backend
backend_container="$(demo_compose ps -aq backend)"
if [[ -n "${backend_container}" ]] \
  && [[ "$(docker inspect --format '{{.State.Running}}' "${backend_container}")" == "true" ]]; then
  demo_fail "backend container did not stop"
fi

reset_started_at="$(date --iso-8601=seconds)"
demo_log "INFO: reset start: candidate=${candidate_generation} startedAt=${reset_started_at}"

failure_stage="image-contract"
demo_require_immutable_image_reference BACKEND_IMAGE "${BACKEND_IMAGE}"
demo_require_immutable_image_reference WEB_IMAGE "${WEB_IMAGE}"
demo_require_immutable_image_reference DEMO_CONTROL_IMAGE "${DEMO_CONTROL_IMAGE}"
[[ "${readiness_timeout_seconds}" =~ ^[1-9][0-9]*$ ]] \
  || demo_fail "DEMO_SMOKE_TIMEOUT_SECONDS must be a positive integer"
((readiness_timeout_seconds >= 30 && readiness_timeout_seconds <= 900)) \
  || demo_fail "DEMO_SMOKE_TIMEOUT_SECONDS must be between 30 and 900"
readiness_attempts="$(((readiness_timeout_seconds + 1) / 2))"

failure_stage="preflight-credential"
preflight_secret="$(demo_tool new-generation | tr -d '\r\n')"
[[ "${preflight_secret}" =~ ^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$ ]] \
  || demo_fail "preflight credential generation failed"
preflight_db_password="preflight-${preflight_secret}"
work_dir="${DEMO_WORK_DIR}/${candidate_generation}"
mapping_file="${work_dir}/stored-files.tsv"

failure_stage="compose-contract"
demo_compose config --quiet
resolved_files_volume="$(docker volume inspect --format '{{.Name}}' "${DEMO_FILES_VOLUME}")"
files_volume_project="$(docker volume inspect \
  --format '{{index .Labels "com.docker.compose.project"}}' "${DEMO_FILES_VOLUME}")"
files_volume_key="$(docker volume inspect \
  --format '{{index .Labels "com.docker.compose.volume"}}' "${DEMO_FILES_VOLUME}")"
[[ "${resolved_files_volume}" == "simple-erp-demo-files" \
  && "${files_volume_project}" == "${DEMO_PROJECT_NAME}" \
  && "${files_volume_key}" == "demo_files" ]] \
  || demo_fail "file volume allowlist mismatch: name=${resolved_files_volume}, project=${files_volume_project}, key=${files_volume_key}"

failure_stage="files-volume-ownership"
demo_prepare_files_volume

failure_stage="retention-pre-prune"
demo_tool cleanup-artifacts \
  --phase pre-reset \
  --files-root /files \
  --state-dir /state \
  --work-root /work \
  --logs-root /logs
pre_prune_completed="true"
mkdir -p -- "${work_dir}"

failure_stage="bundle-validation"
demo_tool validate-bundle \
  --seed-dir /seed \
  --expected-app-version "${DEMO_COMPATIBLE_APP_VERSION}"
actual_seed_version="$(demo_tool manifest-value \
  --seed-dir /seed \
  --key seedVersion | tr -d '\r\n')"
[[ "${actual_seed_version}" == "${DEMO_SEED_EXPECTED_VERSION}" ]] \
  || demo_fail "seed version mismatch: manifest=${actual_seed_version}, expected=${DEMO_SEED_EXPECTED_VERSION}"

failure_stage="control-plane-start"
demo_compose up -d db web
for attempt in $(seq 1 60); do
  db_container="$(demo_compose ps -q db)"
  if [[ -n "${db_container}" ]] \
    && [[ "$(docker inspect --format '{{.State.Health.Status}}' "${db_container}" 2>/dev/null || true)" == "healthy" ]]; then
    break
  fi
  if [[ "${attempt}" == "60" ]]; then
    demo_fail "MariaDB health timeout"
  fi
  sleep 2
done

failure_stage="candidate-database-import"
demo_db_root -e "DROP DATABASE IF EXISTS \`${DEMO_CANDIDATE_DB}\`; CREATE DATABASE \`${DEMO_CANDIDATE_DB}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
candidate_db_present="true"
demo_db_root "${DEMO_CANDIDATE_DB}" < "${DEMO_SEED_DIR}/schema.sql"
demo_db_root "${DEMO_CANDIDATE_DB}" < "${DEMO_SEED_DIR}/seed-data.sql"
demo_db_root <<SQL
  DROP USER IF EXISTS '${DEMO_PREFLIGHT_DB_USER}'@'%';
  CREATE USER '${DEMO_PREFLIGHT_DB_USER}'@'%' IDENTIFIED BY '${preflight_db_password}';
  GRANT SELECT ON \`${DEMO_CANDIDATE_DB}\`.* TO '${DEMO_PREFLIGHT_DB_USER}'@'%';
  FLUSH PRIVILEGES;
SQL
bash "${SCRIPT_DIR}/verify-seed.sh" --database "${DEMO_CANDIDATE_DB}"

failure_stage="candidate-file-staging"
demo_db_root --batch --raw --skip-column-names "${DEMO_CANDIDATE_DB}" -e \
  "SELECT id, stored_name, original_name, content_type, size,
          DATE_FORMAT(created_at, '%Y-%m-%d'),
          DATE_FORMAT((SELECT reset_at FROM demo_seed_manifest WHERE id=1), '%Y-%m-%d'),
          status, owner_type, owner_id, uploader_id
   FROM stored_files ORDER BY id" \
  > "${mapping_file}"
demo_tool stage-files \
  --seed-dir /seed \
  --files-root /files \
  --mapping "/work/${candidate_generation}/stored-files.tsv" \
  --generation "${candidate_generation}" \
  --expected-app-version "${DEMO_COMPATIBLE_APP_VERSION}"
candidate_files_present="true"

failure_stage="candidate-app-preflight"
write_lifecycle_state VERIFYING preflight.json
if docker container inspect "${DEMO_PREFLIGHT_CONTAINER}" >/dev/null 2>&1; then
  docker rm -f "${DEMO_PREFLIGHT_CONTAINER}" >/dev/null
fi
(
  export DB_PASSWORD="${preflight_db_password}"
  demo_compose run -d --no-deps \
    --name "${DEMO_PREFLIGHT_CONTAINER}" \
    -e "DB_URL=jdbc:mariadb://db:3306/${DEMO_CANDIDATE_DB}?characterEncoding=UTF-8&serverTimezone=Asia/Seoul" \
    -e "DB_USERNAME=${DEMO_PREFLIGHT_DB_USER}" \
    -e DB_PASSWORD \
    -e "APP_ADMIN_BOOTSTRAP_ENABLED=false" \
    -e "DEMO_STATE_PATH=/app/data/demo-state/preflight.json" \
    -e "STORAGE_BASE_PATH=/app/data/files/generations/${candidate_generation}" \
    backend >/dev/null
)
preflight_image_id="$(docker inspect --format '{{.Image}}' "${DEMO_PREFLIGHT_CONTAINER}")"
[[ "${preflight_image_id}" =~ ^sha256:[0-9a-f]{64}$ ]] \
  || demo_fail "candidate backend image digest is not immutable: ${preflight_image_id}"
demo_log "INFO: candidate backend image=${preflight_image_id}"
wait_for_preflight_readiness
bash "${SCRIPT_DIR}/smoke-demo.sh" \
  --base-url "http://${DEMO_PREFLIGHT_CONTAINER}:8080" \
  --expected-state VERIFYING \
  --candidate "${candidate_generation}"
capture_container_log "${DEMO_PREFLIGHT_CONTAINER}" \
  "${DEMO_LOG_DIR}/preflight-${candidate_generation}.log"
docker rm -f "${DEMO_PREFLIGHT_CONTAINER}" >/dev/null
demo_db_root -e "DROP USER IF EXISTS '${DEMO_PREFLIGHT_DB_USER}'@'%';"

failure_stage="live-database-promotion"
demo_db_root -e "DROP DATABASE IF EXISTS \`${DEMO_LIVE_DB}\`; CREATE DATABASE \`${DEMO_LIVE_DB}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
demo_db_root "${DEMO_LIVE_DB}" < "${DEMO_SEED_DIR}/schema.sql"
demo_db_root "${DEMO_LIVE_DB}" < "${DEMO_SEED_DIR}/seed-data.sql"
demo_db_root <<'SQL'
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'simple_erp_app'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON `simple_erp_demo`.* TO 'simple_erp_app'@'%';
FLUSH PRIVILEGES;
SQL
bash "${SCRIPT_DIR}/verify-seed.sh" --database "${DEMO_LIVE_DB}"

failure_stage="live-file-promotion"
demo_tool promote-files \
  --seed-dir /seed \
  --files-root /files \
  --mapping "/work/${candidate_generation}/stored-files.tsv" \
  --generation "${candidate_generation}" \
  --expected-app-version "${DEMO_COMPATIBLE_APP_VERSION}"
demo_tool verify-current-files \
  --seed-dir /seed \
  --files-root /files \
  --mapping "/work/${candidate_generation}/stored-files.tsv" \
  --generation "${candidate_generation}" \
  --expected-app-version "${DEMO_COMPATIBLE_APP_VERSION}"

failure_stage="verifying-state"
write_lifecycle_state VERIFYING status.json

failure_stage="live-generation-smoke"
demo_compose up -d --no-deps backend
live_backend_started="true"
live_backend_container="$(demo_compose ps -q backend)"
[[ -n "${live_backend_container}" ]] || demo_fail "live backend container missing"
live_backend_image_id="$(docker inspect --format '{{.Image}}' "${live_backend_container}")"
[[ "${live_backend_image_id}" == "${preflight_image_id}" ]] \
  || demo_fail "preflight/live backend image mismatch: preflight=${preflight_image_id}, live=${live_backend_image_id}"
bash "${SCRIPT_DIR}/smoke-demo.sh" \
  --base-url http://web:8080 \
  --expected-state VERIFYING \
  --candidate "${candidate_generation}"

runtime_employee_count="$(demo_db_root --batch --raw --skip-column-names "${DEMO_LIVE_DB}" -e \
  "SELECT COUNT(*) FROM employees")"
runtime_operator_count="$(demo_db_root --batch --raw --skip-column-names "${DEMO_LIVE_DB}" -e \
  "SELECT COUNT(*) FROM employees e JOIN roles r ON r.id=e.role_id WHERE e.login_id='${APP_ADMIN_LOGIN_ID}' AND r.code='MASTER' AND e.status='ACTIVE'")"
[[ "${runtime_employee_count}" == "23" && "${runtime_operator_count}" == "1" ]] \
  || demo_fail "startupDelta/recovery operator contract mismatch: employees=${runtime_employee_count}, operator=${runtime_operator_count}"

failure_stage="success-cleanup-prerequisites"
demo_db_root -e "DROP DATABASE IF EXISTS \`${DEMO_CANDIDATE_DB}\`;"
candidate_db_present="false"
rm -f -- "${DEMO_STATE_DIR}/preflight.json"

failure_stage="success-retention"
demo_tool cleanup-artifacts \
  --phase post-success \
  --files-root /files \
  --state-dir /state \
  --work-root /work \
  --logs-root /logs \
  --candidate "${candidate_generation}"

failure_stage="ready-promotion"
write_lifecycle_state READY status.json

trap - ERR INT TERM
demo_log "INFO: reset READY: generation=${candidate_generation} image=${live_backend_image_id} nextResetAt=${next_reset_at}"
