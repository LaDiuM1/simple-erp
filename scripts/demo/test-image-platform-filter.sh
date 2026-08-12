#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
filter="${SCRIPT_DIR}/require-linux-arm64.jq"

jq -e -f "${filter}" "${SCRIPT_DIR}/fixtures/imagetools-image-map.json" >/dev/null
jq -e -f "${filter}" "${SCRIPT_DIR}/fixtures/imagetools-image-single.json" >/dev/null

invalid_fixtures=(
  '[]'
  '{}'
  '{"os":"linux"}'
  '{"architecture":"arm64"}'
  '{"os":"linux","architecture":"amd64"}'
  '{"linux/amd64":{"os":"linux","architecture":"amd64"}}'
  '{"linux/arm64":{"os":"linux","architecture":"amd64"}}'
  '{"linux/arm64":"unknown"}'
)
for fixture in "${invalid_fixtures[@]}"; do
  if jq -e -f "${filter}" <<<"${fixture}" >/dev/null; then
    printf '%s\n' "image platform filter accepted invalid fixture: ${fixture}" >&2
    exit 1
  fi
done

printf '%s\n' 'image-platform-filter-ok: linux/arm64 single/map'
