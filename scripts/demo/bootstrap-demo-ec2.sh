#!/usr/bin/env bash

# Prepare the dedicated Simple ERP demo host.
#
# Supported host: Amazon Linux 2023 on aarch64, executed as root. This script
# intentionally does not clone the repository, create secrets, or start the
# application. It only establishes the fail-closed host prerequisites used by
# the separately reviewed deployment procedure.

set -Eeuo pipefail
umask 077

readonly COMPOSE_VERSION="5.4.0"
readonly COMPOSE_MIN_VERSION="2.24.4"
readonly COMPOSE_SHA256="fc5d1371f1ec7987e703da94ede49af3fbfb240b83f22991a98511de7bc4b93b"
readonly COMPOSE_URL="https://github.com/docker/compose/releases/download/v${COMPOSE_VERSION}/docker-compose-linux-aarch64"
readonly BUILDX_VERSION="0.36.1"
readonly BUILDX_SHA256="5d0cafd9d16afe1a0f0d9529885344ace2cc99efdd531b6c783c5455a6001569"
readonly BUILDX_URL="https://github.com/docker/buildx/releases/download/v${BUILDX_VERSION}/buildx-v${BUILDX_VERSION}.linux-arm64"
readonly MIN_DOCKER_ENGINE_VERSION="19.03.0"
readonly PLUGIN_DIR="/usr/local/lib/docker/cli-plugins"
readonly COMPOSE_PATH="${PLUGIN_DIR}/docker-compose"
readonly BUILDX_PATH="${PLUGIN_DIR}/docker-buildx"
readonly PROJECT_ROOT="/opt/simple-erp-demo"
readonly SECRET_ROOT="/etc/simple-erp-demo"
readonly SWAP_PATH="/swapfile"
readonly SWAP_BYTES="1073741824"
readonly REBOOT_MARKER="/run/simple-erp-demo-bootstrap-reboot-required"

temporary_dir=""
swap_temporary_path=""
docker_group_membership_changed="false"
reboot_required="false"

log() {
  printf '%s [simple-erp-bootstrap] %s\n' "$(date --iso-8601=seconds)" "$*"
}

fail() {
  log "ERROR: $*" >&2
  exit 1
}

cleanup() {
  local exit_code="$?"
  if [[ -n "${swap_temporary_path}" && -e "${swap_temporary_path}" ]]; then
    rm -f -- "${swap_temporary_path}"
  fi
  if [[ -n "${temporary_dir}" && -d "${temporary_dir}" ]]; then
    rm -rf -- "${temporary_dir}"
  fi
  exit "${exit_code}"
}
trap cleanup EXIT

version_at_least() {
  local actual="$1"
  local minimum="$2"
  [[ "$(printf '%s\n%s\n' "${minimum}" "${actual}" | sort -V | head -n 1)" == "${minimum}" ]]
}

assert_supported_host() {
  [[ "${EUID}" == "0" ]] || fail "run this script as root"
  [[ "$(uname -s)" == "Linux" ]] || fail "Linux is required"
  [[ "$(uname -m)" == "aarch64" ]] \
    || fail "aarch64 is required; detected $(uname -m)"
  [[ -r /etc/os-release ]] || fail "/etc/os-release is missing"

  # shellcheck disable=SC1091 # The path is fixed by the supported OS contract.
  source /etc/os-release
  [[ "${ID:-}" == "amzn" && "${VERSION_ID:-}" == "2023" ]] \
    || fail "Amazon Linux 2023 is required; detected ${ID:-unknown} ${VERSION_ID:-unknown}"
  command -v dnf >/dev/null 2>&1 || fail "dnf is required"
}

install_signed_os_packages() {
  log "refreshing signed Amazon Linux repositories and applying updates"
  dnf -y --refresh --setopt=gpgcheck=1 upgrade
  dnf -y --setopt=gpgcheck=1 install \
    ca-certificates curl-minimal dnf-utils docker git jq shadow-utils util-linux

  for command_name in \
    awk blkid curl docker fallocate flock getent git gpasswd grep head install jq \
    mkswap mktemp sha256sum sort stat swapon sysctl systemctl tr; do
    command -v "${command_name}" >/dev/null 2>&1 \
      || fail "required command is missing after package installation: ${command_name}"
  done
}

acquire_bootstrap_lock() {
  exec 9>/run/simple-erp-demo-bootstrap.lock
  flock -n 9 || fail "another demo bootstrap process is running"
}

download_verified_asset() {
  local label="$1"
  local url="$2"
  local expected_sha256="$3"
  local destination="$4"
  local download_path="${temporary_dir}/${label}"

  if [[ -f "${destination}" ]] \
    && printf '%s  %s\n' "${expected_sha256}" "${destination}" \
      | sha256sum --check --status; then
    log "${label} already matches the pinned checksum"
    return
  fi

  log "downloading pinned ${label} asset"
  curl --fail --location --silent --show-error \
    --proto '=https' --proto-redir '=https' --tlsv1.2 \
    --retry 3 --retry-all-errors \
    --output "${download_path}" "${url}"
  printf '%s  %s\n' "${expected_sha256}" "${download_path}" \
    | sha256sum --check --status \
    || fail "${label} checksum verification failed"

  install -o root -g root -m 0755 "${download_path}" "${destination}"
  printf '%s  %s\n' "${expected_sha256}" "${destination}" \
    | sha256sum --check --status \
    || fail "${label} installed checksum verification failed"
}

install_pinned_docker_plugins() {
  install -d -o root -g root -m 0755 "${PLUGIN_DIR}"
  temporary_dir="$(mktemp -d /var/tmp/simple-erp-demo-bootstrap.XXXXXX)"

  download_verified_asset \
    "docker-compose" "${COMPOSE_URL}" "${COMPOSE_SHA256}" "${COMPOSE_PATH}"
  download_verified_asset \
    "docker-buildx" "${BUILDX_URL}" "${BUILDX_SHA256}" "${BUILDX_PATH}"
}

ensure_ec2_user_has_no_docker_group() {
  if ! id ec2-user >/dev/null 2>&1 || ! getent group docker >/dev/null 2>&1; then
    return
  fi
  if id -nG ec2-user | tr ' ' '\n' | grep -Fxq docker; then
    log "removing ec2-user from the privileged docker group"
    gpasswd -d ec2-user docker >/dev/null
    docker_group_membership_changed="true"
  fi
  if id -nG ec2-user | tr ' ' '\n' | grep -Fxq docker; then
    fail "ec2-user still belongs to the privileged docker group"
  fi
}

start_and_verify_docker() {
  systemctl enable --now docker
  systemctl is-enabled --quiet docker || fail "docker service is not enabled"
  systemctl is-active --quiet docker || fail "docker service is not active"

  local engine_version docker_os docker_arch compose_version buildx_output
  engine_version="$(docker version --format '{{.Server.Version}}')"
  version_at_least "${engine_version}" "${MIN_DOCKER_ENGINE_VERSION}" \
    || fail "Docker Engine ${engine_version} is older than ${MIN_DOCKER_ENGINE_VERSION}"

  docker_os="$(docker info --format '{{.OSType}}')"
  docker_arch="$(docker info --format '{{.Architecture}}')"
  [[ "${docker_os}" == "linux" ]] || fail "Docker server OS must be linux: ${docker_os}"
  [[ "${docker_arch}" == "aarch64" || "${docker_arch}" == "arm64" ]] \
    || fail "Docker server architecture must be arm64: ${docker_arch}"

  compose_version="$(docker compose version --short)"
  compose_version="${compose_version#v}"
  [[ "${compose_version}" == "${COMPOSE_VERSION}" ]] \
    || fail "Docker Compose version mismatch: ${compose_version}"
  version_at_least "${compose_version}" "${COMPOSE_MIN_VERSION}" \
    || fail "Docker Compose ${compose_version} is older than ${COMPOSE_MIN_VERSION}"

  buildx_output="$(docker buildx version)"
  [[ "${buildx_output}" == *" v${BUILDX_VERSION} "* ]] \
    || fail "Docker Buildx version mismatch: ${buildx_output}"
  docker buildx imagetools inspect --help >/dev/null \
    || fail "Docker Buildx imagetools is unavailable"

  log "Docker ${engine_version}, Compose ${compose_version}, and Buildx ${BUILDX_VERSION} verified"
}

install_fstab_with_swap() {
  local matching_lines canonical_line fstab_copy
  matching_lines="$(awk '$1 == "/swapfile" { count += 1 } END { print count + 0 }' /etc/fstab)"
  if [[ "${matching_lines}" == "0" ]]; then
    fstab_copy="${temporary_dir}/fstab"
    cp --preserve=mode,ownership -- /etc/fstab "${fstab_copy}"
    printf '%s\n' '/swapfile none swap sw 0 0' >> "${fstab_copy}"
    install -o root -g root -m 0644 "${fstab_copy}" /etc/fstab
    return
  fi

  [[ "${matching_lines}" == "1" ]] || fail "multiple /swapfile entries exist in /etc/fstab"
  canonical_line="$(awk '$1 == "/swapfile" { print $1, $2, $3, $4, $5, $6 }' /etc/fstab)"
  [[ "${canonical_line}" == "/swapfile none swap sw 0 0" ]] \
    || fail "the existing /swapfile fstab entry does not match the demo contract"
}

ensure_swap_and_swappiness() {
  local swap_type swap_size sysctl_copy
  if [[ -L "${SWAP_PATH}" ]]; then
    fail "${SWAP_PATH} must not be a symlink"
  fi
  if [[ -e "${SWAP_PATH}" ]]; then
    [[ -f "${SWAP_PATH}" ]] || fail "${SWAP_PATH} must be a regular file"
    swap_size="$(stat -c '%s' "${SWAP_PATH}")"
    [[ "${swap_size}" == "${SWAP_BYTES}" ]] \
      || fail "existing ${SWAP_PATH} size is not exactly 1 GiB"
    swap_type="$(blkid -p -s TYPE -o value "${SWAP_PATH}" 2>/dev/null || true)"
    [[ "${swap_type}" == "swap" ]] \
      || fail "existing ${SWAP_PATH} does not contain a swap signature"
  else
    swap_temporary_path="${SWAP_PATH}.simple-erp-bootstrap.$$"
    [[ ! -e "${swap_temporary_path}" ]] \
      || fail "temporary swap path already exists: ${swap_temporary_path}"
    fallocate -l 1G "${swap_temporary_path}"
    chown root:root "${swap_temporary_path}"
    chmod 0600 "${swap_temporary_path}"
    mkswap "${swap_temporary_path}" >/dev/null
    mv -T -- "${swap_temporary_path}" "${SWAP_PATH}"
    swap_temporary_path=""
  fi

  chown root:root "${SWAP_PATH}"
  chmod 0600 "${SWAP_PATH}"
  install_fstab_with_swap
  if ! swapon --show=NAME --noheadings --raw | grep -Fxq "${SWAP_PATH}"; then
    swapon "${SWAP_PATH}"
  fi
  swapon --show=NAME --noheadings --raw | grep -Fxq "${SWAP_PATH}" \
    || fail "${SWAP_PATH} is not active"

  sysctl_copy="${temporary_dir}/99-simple-erp-demo-swappiness.conf"
  printf '%s\n' 'vm.swappiness=10' > "${sysctl_copy}"
  install -o root -g root -m 0644 \
    "${sysctl_copy}" /etc/sysctl.d/99-simple-erp-demo-swappiness.conf
  sysctl -q -w vm.swappiness=10
  [[ "$(sysctl -n vm.swappiness)" == "10" ]] \
    || fail "vm.swappiness did not converge to 10"

  log "1 GiB swap and vm.swappiness=10 verified"
}

prepare_demo_directories() {
  install -d -o root -g root -m 0755 "${PROJECT_ROOT}"
  install -d -o root -g root -m 0700 "${SECRET_ROOT}"
  [[ "$(stat -c '%U:%G:%a' "${PROJECT_ROOT}")" == "root:root:755" ]] \
    || fail "${PROJECT_ROOT} ownership or mode mismatch"
  [[ "$(stat -c '%U:%G:%a' "${SECRET_ROOT}")" == "root:root:700" ]] \
    || fail "${SECRET_ROOT} ownership or mode mismatch"
}

detect_reboot_requirement() {
  local restart_output restart_status
  dnf -q needs-restarting --help >/dev/null 2>&1 \
    || fail "dnf needs-restarting is unavailable"

  set +e
  restart_output="$(dnf -q needs-restarting -r 2>&1)"
  restart_status="$?"
  set -e
  case "${restart_status}" in
    0)
      reboot_required="false"
      ;;
    1)
      reboot_required="true"
      ;;
    *)
      fail "cannot determine reboot requirement: ${restart_output}"
      ;;
  esac
}

write_final_status() {
  if [[ "${reboot_required}" == "true" \
      || "${docker_group_membership_changed}" == "true" ]]; then
    printf 'reboot_required=%s\ndocker_group_membership_changed=%s\n' \
      "${reboot_required}" "${docker_group_membership_changed}" \
      > "${REBOOT_MARKER}"
    chmod 0600 "${REBOOT_MARKER}"
    log "REBOOT_REQUIRED: reboot the instance, then rerun this script before deployment"
    exit 194
  fi

  rm -f -- "${REBOOT_MARKER}"
  log "BOOTSTRAP_READY: host prerequisites verified; no reboot is pending"
}

main() {
  assert_supported_host
  log "starting Amazon Linux 2023 ARM64 demo bootstrap"
  install_signed_os_packages
  acquire_bootstrap_lock
  install_pinned_docker_plugins
  ensure_ec2_user_has_no_docker_group
  start_and_verify_docker
  ensure_swap_and_swappiness
  prepare_demo_directories
  detect_reboot_requirement
  write_final_status
}

main "$@"
