#!/usr/bin/env bash
# shellcheck disable=SC2034 # Constants are consumed by scripts that source this file.

set -Eeuo pipefail

DEMO_PROJECT_NAME="simple-erp-demo"
DEMO_LIVE_DB="simple_erp_demo"
DEMO_CANDIDATE_DB="simple_erp_demo_candidate"
DEMO_APP_DB_USER="simple_erp_app"
DEMO_PREFLIGHT_DB_USER="simple_erp_preflight"
DEMO_FILES_VOLUME="simple-erp-demo-files"
DEMO_PREFLIGHT_CONTAINER="simple-erp-demo-preflight"
DEMO_PRODUCTION_ROOT="/opt/simple-erp-demo"
DEMO_ROOT_ENV_FILE="/etc/simple-erp-demo/reset.env"
readonly DEMO_CONTROL_IMAGE="python:3.13-alpine@sha256:399babc8b49529dabfd9c922f2b5eea81d611e4512e3ed250d75bd2e7683f4b0"
export DEMO_CONTROL_IMAGE

DEMO_SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
DEMO_PROJECT_ROOT="$(cd -- "${DEMO_SCRIPT_DIR}/../.." && pwd -P)"
DEMO_ENV_FILE="${DEMO_PROJECT_ROOT}/.env.demo"
DEMO_STATE_DIR="${DEMO_PROJECT_ROOT}/runtime/state"
DEMO_WORK_DIR="${DEMO_PROJECT_ROOT}/runtime/work"
DEMO_LOG_DIR="${DEMO_PROJECT_ROOT}/runtime/logs"
DEMO_SEED_DIR="${DEMO_PROJECT_ROOT}/demo/seed"

demo_log() {
  printf '%s %s\n' "$(date --iso-8601=seconds)" "$*"
}

demo_fail() {
  demo_log "ERROR: $*" >&2
  return 1
}

demo_is_true() {
  [[ "${1:-}" == "true" || "${1:-}" == "1" ]]
}

demo_is_immutable_image_reference() {
  local reference="${1:-}"
  [[ "${reference}" =~ ^sha256:[0-9a-f]{64}$ \
    || "${reference}" =~ ^[^@[:space:]]+@sha256:[0-9a-f]{64}$ ]]
}

demo_require_immutable_image_reference() {
  local name="$1"
  local reference="$2"
  demo_is_immutable_image_reference "${reference}" \
    || demo_fail "${name} must be a registry digest or local image ID"
}

demo_assert_project_root() {
  local resolved_expected
  if demo_is_true "${DEMO_TEST_MODE:-false}"; then
    : "${DEMO_TEST_PROJECT_ROOT:?DEMO_TEST_PROJECT_ROOT is required in test mode}"
    resolved_expected="$(cd -- "${DEMO_TEST_PROJECT_ROOT}" && pwd -P)"
    [[ "${DEMO_PROJECT_ROOT}" == "${resolved_expected}" ]] \
      || demo_fail "test project root mismatch: ${DEMO_PROJECT_ROOT} != ${resolved_expected}"
  else
    [[ "${DEMO_PROJECT_ROOT}" == "${DEMO_PRODUCTION_ROOT}" ]] \
      || demo_fail "production reset root allowlist mismatch: ${DEMO_PROJECT_ROOT}"
  fi
}

demo_require_private_file() {
  local path="$1"
  [[ -f "${path}" ]] || demo_fail "required file missing: ${path}"
  [[ ! -L "${path}" ]] || demo_fail "private file must not be a symlink: ${path}"
  if ! demo_is_true "${DEMO_TEST_MODE:-false}"; then
    local owner mode
    owner="$(stat -c '%u' "${path}")"
    mode="$(stat -c '%a' "${path}")"
    [[ "${owner}" == "0" ]] || demo_fail "root-owned file required: ${path}"
    [[ "${mode}" == "600" || "${mode}" == "400" ]] \
      || demo_fail "private file mode must be 600 or 400: ${path} (${mode})"
  fi
}

demo_assert_root_secret_file_contract() {
  local path="$1"
  local line key_count=0
  while IFS= read -r line || [[ -n "${line}" ]]; do
    line="${line#"${line%%[![:space:]]*}"}"
    [[ -z "${line}" || "${line}" == \#* ]] && continue
    [[ "${line}" == DEMO_DB_ROOT_PASSWORD=* ]] \
      || demo_fail "reset secret file may define only DEMO_DB_ROOT_PASSWORD"
    ((key_count += 1))
  done < "${path}"
  [[ "${key_count}" == "1" ]] \
    || demo_fail "reset secret file must define DEMO_DB_ROOT_PASSWORD exactly once"
}

demo_load_environment() {
  demo_require_private_file "${DEMO_ENV_FILE}"
  if grep -Eq '^[[:space:]]*(export[[:space:]]+)?DEMO_DB_ROOT_PASSWORD[[:space:]]*=' \
    "${DEMO_ENV_FILE}"; then
    demo_fail "DEMO_DB_ROOT_PASSWORD must not be stored in .env.demo"
  fi
  set -a
  # shellcheck disable=SC1090
  source "${DEMO_ENV_FILE}"
  set +a

  if demo_is_true "${DEMO_TEST_MODE:-false}"; then
    : "${DEMO_DB_ROOT_PASSWORD:?DEMO_DB_ROOT_PASSWORD is required in test mode}"
  else
    local root_env="${DEMO_RESET_ENV_FILE:-${DEMO_ROOT_ENV_FILE}}"
    [[ "${root_env}" == "${DEMO_ROOT_ENV_FILE}" ]] \
      || demo_fail "reset credential path allowlist mismatch: ${root_env}"
    demo_require_private_file "${root_env}"
    demo_assert_root_secret_file_contract "${root_env}"
  fi

  : "${DB_PASSWORD:?DB_PASSWORD is required}"
  : "${DEMO_DB_ROOT_PASSWORD:?DEMO_DB_ROOT_PASSWORD is required}"
  : "${JWT_SECRET:?JWT_SECRET is required}"
  : "${CORS_ALLOWED_ORIGINS:?CORS_ALLOWED_ORIGINS is required}"
  : "${APP_ADMIN_LOGIN_ID:?APP_ADMIN_LOGIN_ID is required}"
  : "${APP_ADMIN_PASSWORD:?APP_ADMIN_PASSWORD is required}"
  : "${DEMO_SEED_EXPECTED_VERSION:?DEMO_SEED_EXPECTED_VERSION is required}"
  : "${DEMO_COMPATIBLE_APP_VERSION:?DEMO_COMPATIBLE_APP_VERSION is required}"
  : "${BACKEND_IMAGE:?BACKEND_IMAGE is required}"
  : "${WEB_IMAGE:?WEB_IMAGE is required}"
  : "${SITE_ADDRESS:?SITE_ADDRESS is required}"

  [[ "${DB_NAME:-${DEMO_LIVE_DB}}" == "${DEMO_LIVE_DB}" ]] \
    || demo_fail "DB name allowlist mismatch: ${DB_NAME:-}"
  [[ -z "${COMPOSE_PROJECT_NAME:-}" || "${COMPOSE_PROJECT_NAME}" == "${DEMO_PROJECT_NAME}" ]] \
    || demo_fail "Compose project allowlist mismatch: ${COMPOSE_PROJECT_NAME}"
  [[ "${DB_PASSWORD}" != "${DEMO_DB_ROOT_PASSWORD}" ]] \
    || demo_fail "app DB password and reset root password must differ"
  [[ "${APP_ADMIN_LOGIN_ID}" =~ ^[A-Za-z0-9._-]{3,100}$ ]] \
    || demo_fail "recovery operator login format is not safe for reset verification"
  [[ "${APP_ADMIN_LOGIN_ID}" != "demo.manager" && "${APP_ADMIN_LOGIN_ID}" != "demo.staff" ]] \
    || demo_fail "recovery operator login must not reuse a demo account"
}

demo_init() {
  demo_assert_project_root
  demo_load_environment
  command -v docker >/dev/null || demo_fail "docker command missing"
  docker compose version >/dev/null || demo_fail "Docker Compose v2 missing"
  cd -- "${DEMO_PROJECT_ROOT}"
  mkdir -p -- "${DEMO_STATE_DIR}" "${DEMO_WORK_DIR}" "${DEMO_LOG_DIR}"
  chmod 0755 -- "${DEMO_PROJECT_ROOT}/runtime" "${DEMO_STATE_DIR}"
  chmod 0700 -- "${DEMO_WORK_DIR}" "${DEMO_LOG_DIR}"
  if [[ "$(uname -s)" == MINGW* ]]; then
    export MSYS_NO_PATHCONV=1
  fi
}

demo_compose() {
  docker compose \
    --project-name "${DEMO_PROJECT_NAME}" \
    --env-file .env.demo \
    -f compose.yml \
    -f compose.demo.yml \
    "$@"
}

demo_tool() {
  demo_compose run --rm --no-deps -T demo-tool "$@"
}

demo_smoke_tool() {
  demo_compose run --rm --no-deps -T demo-tool-smoke "$@"
}

demo_db_root() {
  (
    export MYSQL_PWD="${DEMO_DB_ROOT_PASSWORD}"
    demo_compose exec -T -e MYSQL_PWD db mariadb -uroot "$@"
  )
}

demo_prepare_files_volume() {
  docker run --rm --pull never --network none --read-only --log-driver none \
    --user 0:10001 --cap-drop ALL --cap-add CHOWN --cap-add FOWNER \
    --security-opt no-new-privileges:true \
    -v "${DEMO_FILES_VOLUME}:/files" \
    --entrypoint sh \
    "${DEMO_CONTROL_IMAGE}" \
    -euc '
      owner="$(stat -c "%u:%g" /files)"
      mode="$(stat -c "%a" /files)"
      if [ "${owner}:${mode}" = "0:0:755" ]; then
        [ -z "$(find /files -mindepth 1 -maxdepth 1 -print -quit)" ] \
          || { echo "fresh demo files volume is not empty" >&2; exit 1; }
        chown 10001:10001 /files
        chmod 2775 /files
      fi
      [ "$(stat -c "%u:%g:%a" /files)" = "10001:10001:2775" ] \
        || { echo "demo files volume ownership contract mismatch" >&2; exit 1; }
    '
}

demo_next_reset_at() {
  if demo_is_true "${DEMO_TEST_MODE:-false}"; then
    date -d "+21600 seconds" --iso-8601=seconds
    return
  fi

  command -v systemctl >/dev/null || demo_fail "systemctl is required outside test mode"
  local raw
  raw="$(systemctl show simple-erp-demo-reset.timer \
    --property=NextElapseUSecRealtime --value 2>/dev/null || true)"
  [[ -n "${raw}" && "${raw}" != "n/a" ]] \
    || demo_fail "active systemd reset timer has no next execution time"
  date -d "${raw}" --iso-8601=seconds
}

demo_acquire_lock() {
  local lock_file="${DEMO_PROJECT_ROOT}/runtime/reset.lock"
  if command -v flock >/dev/null; then
    exec 9>"${lock_file}"
    if ! flock -n 9; then
      demo_log "INFO: another reset holds ${lock_file}; skipping"
      exit 75
    fi
    DEMO_LOCK_KIND="flock"
  elif demo_is_true "${DEMO_TEST_MODE:-false}"; then
    DEMO_TEST_LOCK_DIR="${lock_file}.d"
    if ! mkdir -- "${DEMO_TEST_LOCK_DIR}" 2>/dev/null; then
      demo_log "INFO: another test reset holds ${DEMO_TEST_LOCK_DIR}; skipping"
      exit 75
    fi
    DEMO_LOCK_KIND="mkdir"
  else
    demo_fail "flock is required outside test mode"
  fi
}

demo_release_lock() {
  if [[ "${DEMO_LOCK_KIND:-}" == "mkdir" && -n "${DEMO_TEST_LOCK_DIR:-}" ]]; then
    rmdir -- "${DEMO_TEST_LOCK_DIR}" 2>/dev/null || true
  fi
}
