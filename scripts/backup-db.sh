#!/usr/bin/env bash
# 야간 mysqldump → S3 (gzip + STANDARD_IA).
# /opt/simple-erp/ 에서 .env 를 읽고 docker compose 의 db 컨테이너로 dump.
#
# cron 등록 (매일 KST 03:00):
#   sudo crontab -e
#   0 3 * * * /opt/simple-erp/scripts/backup-db.sh >> /var/log/simple-erp-backup.log 2>&1
#
# S3 라이프사이클으로 30일 후 삭제 권장 (콘솔에서 1회 설정).

set -euo pipefail

cd "$(dirname "$0")/.."

# .env 로드
if [[ ! -f .env ]]; then
  echo "$(date -Iseconds) ERROR: .env 없음 — /opt/simple-erp/.env 확인" >&2
  exit 1
fi
set -a; source .env; set +a

: "${S3_BUCKET:?S3_BUCKET 미설정}"
: "${DB_PASSWORD:?DB_PASSWORD 미설정}"
: "${DB_NAME:=simple_erp}"

DATE="$(date +%Y%m%d_%H%M%S)"
DUMP_FILE="/tmp/${DB_NAME}_${DATE}.sql.gz"

echo "$(date -Iseconds) INFO: dump 시작"
# MariaDB 11 부터 mysqldump 가 mariadb-dump 로 이름 변경됨 (호환 심볼릭 링크 미제공)
docker compose exec -T db mariadb-dump \
  -u root -p"${DB_PASSWORD}" \
  --single-transaction \
  --routines \
  --triggers \
  --no-tablespaces \
  "${DB_NAME}" | gzip > "${DUMP_FILE}"

SIZE="$(stat -c%s "${DUMP_FILE}" 2>/dev/null || stat -f%z "${DUMP_FILE}")"
echo "$(date -Iseconds) INFO: dump 완료 (${SIZE} bytes)"

aws s3 cp "${DUMP_FILE}" "s3://${S3_BUCKET}/backups/${DB_NAME}_${DATE}.sql.gz" \
  --storage-class STANDARD_IA \
  --no-progress

rm -f "${DUMP_FILE}"
echo "$(date -Iseconds) INFO: ✓ S3 업로드 완료 — s3://${S3_BUCKET}/backups/${DB_NAME}_${DATE}.sql.gz"
