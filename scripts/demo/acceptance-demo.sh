#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=scripts/demo/lib.sh
source "${SCRIPT_DIR}/lib.sh"
# shellcheck source=scripts/demo/acceptance_traps.sh
source "${SCRIPT_DIR}/acceptance_traps.sh"

demo_init
DEMO_ENV_LOADED=1
export DEMO_ENV_LOADED

cleanup_required="false"
exercise_succeeded="false"
marker=""
previous_generation=""
customer_id=""
excel_customer_id=""
sales_contact_id=""
contract_id=""
equipment_id=""
settled_contract_id=""
settled_equipment_id=""
after_service_id=""
board_id=""
expense_id=""
approval_id=""
manager_id=""
staff_id=""
board_file_id=""
approval_file_id=""
expense_file_id=""
pending_file_id=""
drive_file_id=""
drive_storage_id=""
attendance_date=""
declare -A file_stored_names=()
declare -A file_relative_paths=()

finalize_acceptance() {
  local original_exit="$?"
  local reset_exit=0
  local verify_exit=0
  local generation_exit=0
  trap - EXIT INT TERM

  if [[ "${cleanup_required}" == "true" ]]; then
    demo_log "acceptance cleanup reset start"
    set +e
    "${SCRIPT_DIR}/reset-demo.sh"
    reset_exit="$?"
    set -e
    if ((reset_exit != 0)); then
      demo_log "ERROR: acceptance cleanup reset failed: exit=${reset_exit}" >&2
      exit "${reset_exit}"
    fi

    if [[ "${exercise_succeeded}" == "true" ]]; then
      set +e
      demo_smoke_tool acceptance \
        --seed-dir /seed \
        --base-url http://web:8080 \
        --phase verify-reset \
        --marker "${marker}" \
        --previous-generation "${previous_generation}" \
        --customer-id "${customer_id}" \
        --excel-customer-id "${excel_customer_id}" \
        --sales-contact-id "${sales_contact_id}" \
        --contract-id "${contract_id}" \
        --equipment-id "${equipment_id}" \
        --settled-contract-id "${settled_contract_id}" \
        --settled-equipment-id "${settled_equipment_id}" \
        --after-service-id "${after_service_id}" \
        --board-id "${board_id}" \
        --expense-id "${expense_id}" \
        --approval-id "${approval_id}" \
        --board-file-id "${board_file_id}" \
        --approval-file-id "${approval_file_id}" \
        --expense-file-id "${expense_file_id}" \
        --pending-file-id "${pending_file_id}" \
        --drive-file-id "${drive_file_id}" \
        --attendance-date "${attendance_date}" \
        --expected-app-version "${DEMO_COMPATIBLE_APP_VERSION}" \
        --timeout-seconds "${DEMO_SMOKE_TIMEOUT_SECONDS:-120}"
      verify_exit="$?"
      set -e
      if ((verify_exit != 0)); then
        demo_log "ERROR: acceptance cleanup verification failed: exit=${verify_exit}" >&2
        exit "${verify_exit}"
      fi

      set +e
      assert_acceptance_file_rows_absent
      verify_exit="$?"
      set -e
      if ((verify_exit != 0)); then
        demo_log "ERROR: acceptance stored file rows survived reset" >&2
        exit "${verify_exit}"
      fi

      set +e
      demo_tool assert-generation-absent \
        --files-root /files \
        --generation "${previous_generation}"
      generation_exit="$?"
      set -e
      if ((generation_exit != 0)); then
        demo_log "ERROR: acceptance uploaded file generation was retained: exit=${generation_exit}" >&2
        exit "${generation_exit}"
      fi
    fi
  fi

  exit "${original_exit}"
}

stored_file_row() {
  local file_id="$1"
  [[ "${file_id}" =~ ^[1-9][0-9]*$ ]] \
    || demo_fail "stored file evidence ID is invalid: ${file_id}"
  demo_db_root --batch --skip-column-names "${DEMO_LIVE_DB}" -e \
    "SELECT id, stored_name, DATE_FORMAT(created_at, '%Y/%m'), status, COALESCE(owner_type, 'NULL'), COALESCE(owner_id, 0), COALESCE(uploader_id, 0), size FROM stored_files WHERE id=${file_id};"
}

capture_file_evidence() {
  local prefix="$1"
  local file_id="$2"
  local expected_status="$3"
  local expected_owner_type="$4"
  local expected_owner_id="$5"
  local expected_uploader_id="$6"
  local row=""
  local actual_id="" stored_name="" created_month="" status=""
  local owner_type="" owner_id="" uploader_id="" size="" extra=""

  row="$(stored_file_row "${file_id}")"
  IFS=$'\t' read -r actual_id stored_name created_month status owner_type owner_id uploader_id size extra <<<"${row}"
  [[ -z "${extra}" \
      && "${actual_id}" == "${file_id}" \
      && "${stored_name}" =~ ^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$ \
      && "${created_month}" =~ ^20[0-9]{2}/(0[1-9]|1[0-2])$ \
      && "${status}" == "${expected_status}" \
      && "${owner_type}" == "${expected_owner_type}" \
      && "${owner_id}" == "${expected_owner_id}" \
      && "${uploader_id}" == "${expected_uploader_id}" \
      && "${size}" =~ ^[1-9][0-9]*$ ]] \
    || demo_fail "stored file evidence mismatch: prefix=${prefix} id=${file_id} row=${row}"

  file_stored_names["${prefix}"]="${stored_name}"
  file_relative_paths["${prefix}"]="${created_month}/${stored_name}"
}

assert_file_evidence() {
  local prefix="$1"
  local file_id="$2"
  local expected_status="$3"
  local expected_owner_type="$4"
  local expected_owner_id="$5"
  local expected_uploader_id="$6"
  local expected_stored="${file_stored_names[${prefix}]}"
  local expected_relative="${file_relative_paths[${prefix}]}"

  capture_file_evidence \
    "${prefix}" "${file_id}" "${expected_status}" "${expected_owner_type}" \
    "${expected_owner_id}" "${expected_uploader_id}"
  [[ "${file_stored_names[${prefix}]}" == "${expected_stored}" \
      && "${file_relative_paths[${prefix}]}" == "${expected_relative}" ]] \
    || demo_fail "stored file identity changed: prefix=${prefix} id=${file_id}"
}

verify_acceptance_file_bodies() {
  local kind relative_path
  for kind in board approval expense pending drive; do
    relative_path="${file_relative_paths[${kind}]}"
    demo_tool verify-acceptance-file \
      --files-root /files \
      --generation "${previous_generation}" \
      --relative-path "${relative_path}" \
      --marker "${marker}" \
      --kind "${kind}"
  done
}

assert_acceptance_file_rows_absent() {
  local count
  count="$(demo_db_root --batch --skip-column-names "${DEMO_LIVE_DB}" -e \
    "SELECT COUNT(*) FROM stored_files WHERE id IN (${board_file_id}, ${approval_file_id}, ${expense_file_id}, ${pending_file_id}, ${drive_storage_id});")"
  [[ "${count}" == "0" ]] \
    || demo_fail "reset retained acceptance stored file rows: count=${count}"
}

demo_install_acceptance_traps finalize_acceptance

# Start from the canonical seed so a prior demo session cannot make the exercise flaky.
"${SCRIPT_DIR}/reset-demo.sh"
cleanup_required="true"

marker_generation="$(demo_smoke_tool new-generation)"
[[ "${marker_generation}" =~ ^[0-9a-f-]{36}$ ]] \
  || demo_fail "acceptance marker generation format mismatch"
marker="demo-it-${marker_generation//-/}"

exercise_output="$(demo_compose run --rm --no-deps -T \
  -e APP_ADMIN_LOGIN_ID \
  -e APP_ADMIN_PASSWORD \
  demo-tool-smoke acceptance \
  --seed-dir /seed \
  --base-url http://web:8080 \
  --phase exercise \
  --marker "${marker}" \
  --verify-operator-protection \
  --expected-app-version "${DEMO_COMPATIBLE_APP_VERSION}" \
  --timeout-seconds "${DEMO_SMOKE_TIMEOUT_SECONDS:-120}")"
printf '%s\n' "${exercise_output}"

acceptance_field() {
  local name="$1"
  sed -nE "s/^acceptance-exercise-ok: .* ${name}=([^ ]+).*/\\1/p" <<<"${exercise_output}"
}

previous_generation="$(acceptance_field generation)"
customer_id="$(acceptance_field customer)"
excel_customer_id="$(acceptance_field excelCustomer)"
sales_contact_id="$(acceptance_field salesContact)"
contract_id="$(acceptance_field contract)"
equipment_id="$(acceptance_field equipment)"
settled_contract_id="$(acceptance_field settledContract)"
settled_equipment_id="$(acceptance_field settledEquipment)"
after_service_id="$(acceptance_field afterService)"
board_id="$(acceptance_field board)"
expense_id="$(acceptance_field expense)"
approval_id="$(acceptance_field approval)"
manager_id="$(acceptance_field managerId)"
staff_id="$(acceptance_field staffId)"
board_file_id="$(acceptance_field boardFile)"
approval_file_id="$(acceptance_field approvalFile)"
expense_file_id="$(acceptance_field expenseFile)"
pending_file_id="$(acceptance_field pendingFile)"
drive_file_id="$(acceptance_field driveFile)"
attendance_date="$(acceptance_field attendanceDate)"
[[ "${previous_generation}" =~ ^[0-9a-f-]{36}$ ]] \
  || demo_fail "acceptance exercise generation result missing"
for created_id in \
  "${customer_id}" "${contract_id}" "${equipment_id}" \
  "${excel_customer_id}" "${sales_contact_id}" \
  "${settled_contract_id}" "${settled_equipment_id}" "${after_service_id}" \
  "${board_id}" "${expense_id}" "${approval_id}" \
  "${manager_id}" "${staff_id}" \
  "${board_file_id}" "${approval_file_id}" "${expense_file_id}" \
  "${pending_file_id}" "${drive_file_id}"; do
  [[ "${created_id}" =~ ^[1-9][0-9]*$ ]] \
    || demo_fail "acceptance exercise created ID result missing"
done
[[ "${attendance_date}" =~ ^[0-9]{4}-[0-9]{2}-[0-9]{2}$ ]] \
  || demo_fail "acceptance exercise attendance date result missing"

drive_storage_id="$(demo_db_root --batch --skip-column-names "${DEMO_LIVE_DB}" -e \
  "SELECT storage_file_id FROM drive_files WHERE id=${drive_file_id};")"
[[ "${drive_storage_id}" =~ ^[1-9][0-9]*$ ]] \
  || demo_fail "acceptance Drive storage file ID evidence missing"

capture_file_evidence board "${board_file_id}" CLAIMED BOARD_POST "${board_id}" "${staff_id}"
capture_file_evidence approval "${approval_file_id}" CLAIMED APPROVAL_DOCUMENT "${approval_id}" "${staff_id}"
capture_file_evidence expense "${expense_file_id}" CLAIMED EXPENSE_CLAIM "${expense_id}" "${staff_id}"
capture_file_evidence pending "${pending_file_id}" PENDING NULL 0 "${manager_id}"
capture_file_evidence drive "${drive_storage_id}" CLAIMED DRIVE_FILE "${drive_file_id}" "${manager_id}"

printf '%s\n' \
  "acceptance-file-evidence: boardFile=${board_file_id} boardStored=${file_stored_names[board]} approvalFile=${approval_file_id} approvalStored=${file_stored_names[approval]} expenseFile=${expense_file_id} expenseStored=${file_stored_names[expense]} pendingFile=${pending_file_id} pendingStored=${file_stored_names[pending]} driveFile=${drive_file_id} driveStorageFile=${drive_storage_id} driveStored=${file_stored_names[drive]}"

demo_smoke_tool acceptance \
  --seed-dir /seed \
  --base-url http://web:8080 \
  --phase delete-retained \
  --marker "${marker}" \
  --previous-generation "${previous_generation}" \
  --board-id "${board_id}" \
  --board-file-id "${board_file_id}" \
  --drive-file-id "${drive_file_id}" \
  --expected-app-version "${DEMO_COMPATIBLE_APP_VERSION}" \
  --timeout-seconds "${DEMO_SMOKE_TIMEOUT_SECONDS:-120}"

assert_file_evidence board "${board_file_id}" DELETE_PENDING BOARD_POST "${board_id}" "${staff_id}"
assert_file_evidence approval "${approval_file_id}" CLAIMED APPROVAL_DOCUMENT "${approval_id}" "${staff_id}"
assert_file_evidence expense "${expense_file_id}" CLAIMED EXPENSE_CLAIM "${expense_id}" "${staff_id}"
assert_file_evidence pending "${pending_file_id}" PENDING NULL 0 "${manager_id}"
assert_file_evidence drive "${drive_storage_id}" DELETE_PENDING DRIVE_FILE "${drive_file_id}" "${manager_id}"
verify_acceptance_file_bodies
exercise_succeeded="true"

# READY must remain restartable after visitor files have made the current generation mutable.
demo_compose restart backend
bash "${SCRIPT_DIR}/smoke-demo.sh" \
  --base-url http://web:8080 \
  --expected-state READY \
  --candidate "${previous_generation}"
demo_smoke_tool acceptance \
  --seed-dir /seed \
  --base-url http://web:8080 \
  --phase verify-live \
  --marker "${marker}" \
  --previous-generation "${previous_generation}" \
  --customer-id "${customer_id}" \
  --excel-customer-id "${excel_customer_id}" \
  --sales-contact-id "${sales_contact_id}" \
  --contract-id "${contract_id}" \
  --equipment-id "${equipment_id}" \
  --settled-contract-id "${settled_contract_id}" \
  --settled-equipment-id "${settled_equipment_id}" \
  --after-service-id "${after_service_id}" \
  --board-id "${board_id}" \
  --expense-id "${expense_id}" \
  --approval-id "${approval_id}" \
  --board-file-id "${board_file_id}" \
  --approval-file-id "${approval_file_id}" \
  --expense-file-id "${expense_file_id}" \
  --pending-file-id "${pending_file_id}" \
  --drive-file-id "${drive_file_id}" \
  --attendance-date "${attendance_date}" \
  --expected-app-version "${DEMO_COMPATIBLE_APP_VERSION}" \
  --timeout-seconds "${DEMO_SMOKE_TIMEOUT_SECONDS:-120}"

assert_file_evidence board "${board_file_id}" DELETE_PENDING BOARD_POST "${board_id}" "${staff_id}"
assert_file_evidence approval "${approval_file_id}" CLAIMED APPROVAL_DOCUMENT "${approval_id}" "${staff_id}"
assert_file_evidence expense "${expense_file_id}" CLAIMED EXPENSE_CLAIM "${expense_id}" "${staff_id}"
assert_file_evidence pending "${pending_file_id}" PENDING NULL 0 "${manager_id}"
assert_file_evidence drive "${drive_storage_id}" DELETE_PENDING DRIVE_FILE "${drive_file_id}" "${manager_id}"
verify_acceptance_file_bodies
