#!/usr/bin/env bash
# MariaDB entrypoint가 생성한 app user의 광범위 권한을 live schema DML로 축소한다.

set -euo pipefail

if [[ "${MARIADB_DATABASE:-}" != "simple_erp_demo" ]]; then
  echo "demo DB allowlist mismatch" >&2
  exit 1
fi
if [[ "${MARIADB_USER:-}" != "simple_erp_app" || "${MARIADB_USER_HOST:-%}" != "%" ]]; then
  echo "demo app DB user allowlist mismatch" >&2
  exit 1
fi
: "${MARIADB_ROOT_PASSWORD:?MARIADB_ROOT_PASSWORD is required}"

MYSQL_PWD="${MARIADB_ROOT_PASSWORD}" mariadb --protocol=socket -uroot <<'SQL'
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'simple_erp_app'@'%';
GRANT SELECT, INSERT, UPDATE, DELETE ON `simple_erp_demo`.* TO 'simple_erp_app'@'%';
FLUSH PRIVILEGES;
SQL

echo "demo app DB user privileges reduced to live-schema DML"
