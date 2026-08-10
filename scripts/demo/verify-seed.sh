#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=scripts/demo/lib.sh
source "${SCRIPT_DIR}/lib.sh"

database=""
while (($#)); do
  case "$1" in
    --database)
      database="${2:-}"
      shift 2
      ;;
    *)
      demo_fail "unknown argument: $1"
      ;;
  esac
done

[[ "${database}" == "${DEMO_LIVE_DB}" || "${database}" == "${DEMO_CANDIDATE_DB}" ]] \
  || demo_fail "database allowlist mismatch: ${database}"

if [[ -z "${DEMO_ENV_LOADED:-}" ]]; then
  demo_init
  DEMO_ENV_LOADED=1
fi

violations="$(demo_db_root --batch --raw --skip-column-names "${database}" \
  < "${DEMO_SEED_DIR}/verify-seed.sql")"
if [[ -n "${violations}" ]]; then
  printf '%s\n' "${violations}" >&2
  demo_fail "seed verification returned violation rows for ${database}"
fi

demo_log "INFO: seed verification passed with zero rows: ${database}"

manifest_generated_at="$(demo_tool manifest-value \
  --seed-dir /seed --key generatedAt | tr -d '\r\n')"
manifest_source_epoch="$(demo_tool manifest-value \
  --seed-dir /seed --key sourceDateEpoch | tr -d '\r\n')"
db_manifest_contract="$(demo_db_root --batch --raw --skip-column-names "${database}" -e \
  "SET time_zone='+00:00';
   SELECT DATE_FORMAT(generated_at, '%Y-%m-%dT%H:%i:%s+09:00'),
          CAST(UNIX_TIMESTAMP(CONVERT_TZ(generated_at, '+09:00', '+00:00')) AS UNSIGNED)
   FROM demo_seed_manifest
   WHERE id=1")"
IFS=$'\t' read -r db_generated_at db_source_epoch <<< "${db_manifest_contract}"
[[ "${db_generated_at}" == "${manifest_generated_at}" \
  && "${db_source_epoch}" == "${manifest_source_epoch}" ]] \
  || demo_fail "DB/manifest generatedAt mismatch: database=${db_generated_at}/${db_source_epoch}, manifest=${manifest_generated_at}/${manifest_source_epoch}"

demo_log "INFO: DB/manifest generatedAt contract passed: ${database} ${db_generated_at}"
