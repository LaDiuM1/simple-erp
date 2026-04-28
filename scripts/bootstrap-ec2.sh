#!/usr/bin/env bash
# t4g.small + Amazon Linux 2023 (ARM64) 첫 1회 실행.
# 사용법:
#   scp -i key.pem scripts/bootstrap-ec2.sh ec2-user@<eip>:~
#   ssh -i key.pem ec2-user@<eip>
#   sudo bash bootstrap-ec2.sh

set -euo pipefail

if [[ $EUID -ne 0 ]]; then
  echo "root 권한 필요 — sudo 로 실행"
  exit 1
fi

echo "==> 시스템 업데이트"
dnf -y update

echo "==> Docker 설치"
dnf -y install docker
systemctl enable --now docker
usermod -aG docker ec2-user

echo "==> Docker Compose v2 plugin 설치 (Amazon Linux 2023 패키지에 미포함)"
mkdir -p /usr/local/lib/docker/cli-plugins
curl -fsSL https://github.com/docker/compose/releases/latest/download/docker-compose-linux-aarch64 \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

echo "==> 1GB swap (2GB RAM 보호)"
if [[ ! -f /swapfile ]]; then
  fallocate -l 1G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo "/swapfile none swap sw 0 0" >> /etc/fstab
  # OOM 직전까지 swap 사용 안 하도록 swappiness 낮춤
  sysctl vm.swappiness=10
  echo "vm.swappiness=10" > /etc/sysctl.d/99-swappiness.conf
fi

echo "==> 앱 디렉토리"
mkdir -p /opt/simple-erp/scripts
chown -R ec2-user:ec2-user /opt/simple-erp

echo "==> AWS CLI v2 (백업용)"
if ! command -v aws &>/dev/null; then
  curl -fsSL "https://awscli.amazonaws.com/awscli-exe-linux-aarch64.zip" -o /tmp/awscliv2.zip
  dnf -y install unzip
  unzip -q /tmp/awscliv2.zip -d /tmp
  /tmp/aws/install
  rm -rf /tmp/aws /tmp/awscliv2.zip
fi

echo ""
echo "✓ 부트스트랩 완료."
echo ""
echo "다음 단계:"
echo "  1. /opt/simple-erp/ 에 compose.yml + .env 배치"
echo "     scp -i key.pem compose.yml .env ec2-user@<eip>:/opt/simple-erp/"
echo "  2. scripts/backup-db.sh 도 같이 배치 후 cron 등록"
echo "  3. GHCR private 패키지면: docker login ghcr.io -u <user> -p <PAT>"
echo "  4. docker compose pull && docker compose up -d"
echo "  5. docker compose logs -f backend  으로 health 확인"
echo ""
echo "※ Docker 그룹 변경이 적용되려면 ec2-user 로 다시 로그인 필요"
