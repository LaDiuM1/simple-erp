#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
# shellcheck source=scripts/demo/lib.sh
source "${SCRIPT_DIR}/lib.sh"

base_url=""
expected_state=""
candidate=""
while (($#)); do
  case "$1" in
    --base-url)
      base_url="${2:-}"
      shift 2
      ;;
    --expected-state)
      expected_state="${2:-}"
      shift 2
      ;;
    --candidate)
      candidate="${2:-}"
      shift 2
      ;;
    *)
      demo_fail "unknown argument: $1"
      ;;
  esac
done

[[ "${base_url}" == "http://web" || "${base_url}" == "http://${DEMO_PREFLIGHT_CONTAINER}:8080" ]] \
  || demo_fail "smoke base URL allowlist mismatch: ${base_url}"
[[ "${expected_state}" == "READY" || "${expected_state}" == "VERIFYING" ]] \
  || demo_fail "smoke state allowlist mismatch: ${expected_state}"
[[ "${candidate}" =~ ^[0-9a-f-]{36}$ ]] || demo_fail "candidate generation format mismatch"

if [[ -z "${DEMO_ENV_LOADED:-}" ]]; then
  demo_init
  DEMO_ENV_LOADED=1
fi

demo_smoke_tool smoke \
  --seed-dir /seed \
  --base-url "${base_url}" \
  --expected-state "${expected_state}" \
  --candidate "${candidate}" \
  --expected-app-version "${DEMO_COMPATIBLE_APP_VERSION}" \
  --timeout-seconds "${DEMO_SMOKE_TIMEOUT_SECONDS:-120}"
