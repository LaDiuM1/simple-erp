#!/usr/bin/env bash

demo_acceptance_signal() {
  exit "$1"
}

demo_install_acceptance_traps() {
  local finalize_function="$1"
  [[ "${finalize_function}" =~ ^[a-zA-Z_][a-zA-Z0-9_]*$ ]] \
    || return 64
  # shellcheck disable=SC2064 # 위 allowlist를 통과한 함수 이름을 현재 값으로 고정한다.
  trap "${finalize_function}" EXIT
  trap 'demo_acceptance_signal 130' INT
  trap 'demo_acceptance_signal 143' TERM
}
