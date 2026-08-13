# Simple ERP 데모 시드

이 디렉터리는 실사용 환경과 분리된 데모용 합성 데이터 묶음이다. 고객사, 담당자, 연락처, 등록번호, 파일 내용은 모두 새로 구성한 값이며 실제 회사·개인·거래를 나타내지 않는다. 화면에 표시되는 이름과 업무 기록은 일반적인 ERP 사용 흐름처럼 읽히도록 구성하되, 연락처는 `.example` 도메인과 무효 번호 대역을 사용하고 등록번호도 무효 접두어를 유지한다.

## 구성과 경계

- `seed/schema.sql`: Hibernate `update`와 시작 시점 스키마 마이그레이터를 모두 적용한 뒤 추출한 canonical MariaDB 스키마다.
- `seed/seed-data.sql`: 가져오기 시각을 한 번만 캡처해 최근 영업·계약·AS·결재·근태 시나리오를 만드는 합성 데이터다.
- `seed/seed-files.tar.gz`: `stored_files` 30건과 대응하는 안전한 PDF, XLSX, TXT, PNG 객체다.
- `seed/manifest.json`: 버전, 호환성, 정확한 기대 건수, 데모 계정, 합성 데이터 계약과 파일별 메타데이터·SHA-256을 기록한 폐쇄형 기계 판독 기준이다. 알 수 없는 키나 계약 확장은 검증 실패로 닫힌다.
- `seed/verify-seed.sql`: 테이블 수, 건수, 참조 무결성, 계층 순환, 상태 전이와 자식 cardinality, 날짜·채번 범위, DB 파일 메타데이터, 데모 계정과 합성 데이터 경계를 검사한다. 마지막 조회 결과가 0행이면 통과다.
- `tools/generate_seed.py`: `seed-data.sql`, `seed-files.tar.gz`, `manifest.json`을 결정적으로 생성한다.
- `../scripts/demo/demo_control.py`: 고정 allowlist, candidate 검증, 원자적 상태·파일 승격과 종단 smoke를 구현한 단일 마운트 control plane이다.
- `../scripts/demo/verify_demo_contract.py`: Compose·reset·workflow의 권한, bind mount, immutable image 계약을 로컬과 CI에서 같은 코드로 검증한다.
- `../compose.demo.yml`: 기존 운영 Compose에만 겹쳐 쓰는 독립 데모 overlay다.
- `../ops/systemd/`: 6시간마다 같은 reset 스크립트 하나만 실행하는 one-shot service와 timer다.

스키마의 43개 테이블은 애플리케이션 `@Entity` 테이블 39개, 컬렉션 테이블 2개, Spring Modulith의 `event_publication` 1개, 데모 버전 표식인 `demo_seed_manifest` 1개로 구성된다. 애플리케이션 소유 enum 열은 `VARCHAR`이고 프레임워크 소유 `event_publication.status`만 native `ENUM`이다.

주요 데이터는 직원 22명, 고객 48곳, 제품 32개, 계약 42건, 장비 17대, AS 45건, 결재 36건, 근태 380~450건이다. 계약은 최근 6개월과 전체 상태를, AS는 5개 유형을 모두 포함한다. 정확한 전체 건수와 파일 해시는 `manifest.json`을 단일 기준으로 사용한다.

## 데모 계정

| 역할 | 로그인 ID | 비밀번호 |
| --- | --- | --- |
| 데모 매니저 | `demo.manager` | `ManagerDemo!2026` |
| 데모 직원 | `demo.staff` | `StaffDemo!2026` |

시드에는 위 두 계정만 로그인이 가능한 비밀번호 해시가 있다. 나머지 직원은 대응하는 평문을 저장하지 않는 고정 잠금 해시를 공유한다. `MASTER` 역할과 권한은 포함하지만 복구 운영 계정과 자격 증명은 포함하지 않는다. 애플리케이션 시작 뒤 `EmployeeInitializer`가 `APP_ADMIN_*` 환경 변수로 복구 운영 계정 1명을 만들기 때문에, 시작 후 직원 수는 23명이다. 이 차이는 manifest의 `startupDelta.employees = 1`로 명시한다.

## 생성과 재현성 확인

Python 3.11 이상과 표준 라이브러리만 필요하다.

```bash
python3 demo/tools/generate_seed.py
python3 demo/tools/generate_seed.py --check
```

로컬 Python이 없으면 저장소 루트에서 다음처럼 실행할 수 있다.

```powershell
$demoPythonImage = "python:3.13-alpine@sha256:399babc8b49529dabfd9c922f2b5eea81d611e4512e3ed250d75bd2e7683f4b0"
docker run --rm --network none --read-only --cap-drop ALL `
  --security-opt no-new-privileges:true --tmpfs /tmp:rw,noexec,nosuid,size=16m `
  -e PYTHONDONTWRITEBYTECODE=1 `
  -v "${PWD}/demo/tools:/work/demo/tools:ro" -v "${PWD}/demo/seed:/work/demo/seed" `
  -w /work $demoPythonImage `
  python demo/tools/generate_seed.py
docker run --rm --network none --read-only --cap-drop ALL `
  --security-opt no-new-privileges:true --tmpfs /tmp:rw,noexec,nosuid,size=16m `
  -e PYTHONDONTWRITEBYTECODE=1 `
  -v "${PWD}/demo/tools:/work/demo/tools:ro" -v "${PWD}/demo/seed:/work/demo/seed:ro" `
  -w /work $demoPythonImage `
  python demo/tools/generate_seed.py --check
```

생성기는 UUID, JSON 키 순서와 줄바꿈, TAR/GZIP/ZIP 메타데이터 시각을 고정한다. 기본 `generatedAt`은 `2026-08-08T00:00:00+09:00`이다. 다른 재현 가능한 기준 시각이 필요하면 `SOURCE_DATE_EPOCH`을 설정하면 되며, 같은 값에서는 세 산출물이 byte-for-byte 같아야 한다. 생성 산출물을 직접 편집하지 않는다.

`schemaSourceCommit`은 canonical 스키마를 추출한 커밋의 증거이고, `compatibleAppVersion`은 해당 스키마·시드 계약을 소비할 수 있는 애플리케이션 버전이다. 실행 중인 배포의 커밋이나 이미지 digest를 대신하지 않는다.

## MariaDB 가져오기와 검증

아래 예시는 별도 MariaDB 11.8.6 컨테이너에서 번들을 검증한다. 저장소 루트에서 실행하고, 서버가 연결을 받을 준비가 된 뒤 가져오기를 진행한다.

```powershell
$env:MARIADB_ROOT_PASSWORD = "로컬-검증용-비밀번호"
docker run --name simple-erp-seed-check --detach `
  -e MARIADB_ROOT_PASSWORD mariadb:11.8.6@sha256:78a5047d3ba33975f183f183c2464cc7f1eab13ec8667e57cc9a5821d6da7577

docker cp demo/seed/schema.sql simple-erp-seed-check:/tmp/schema.sql
docker cp demo/seed/seed-data.sql simple-erp-seed-check:/tmp/seed-data.sql
docker cp demo/seed/verify-seed.sql simple-erp-seed-check:/tmp/verify-seed.sql

$env:MYSQL_PWD = $env:MARIADB_ROOT_PASSWORD
docker exec -e MYSQL_PWD simple-erp-seed-check mariadb -uroot -e `
  "CREATE DATABASE simple_erp_demo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"
docker exec -e MYSQL_PWD simple-erp-seed-check sh -lc `
  "mariadb -uroot simple_erp_demo < /tmp/schema.sql && mariadb -uroot simple_erp_demo < /tmp/seed-data.sql"
docker exec -e MYSQL_PWD simple-erp-seed-check sh -lc `
  "mariadb --batch --raw --skip-column-names -uroot simple_erp_demo < /tmp/verify-seed.sql"
Remove-Item Env:MARIADB_ROOT_PASSWORD, Env:MYSQL_PWD
```

마지막 명령이 아무 행도 출력하지 않아야 한다. 출력 행은 `check_name`, `record_id`, `detail` 순서의 위반 내역이다. 시드는 빈 canonical 스키마에 한 번 가져오는 것을 전제로 한다.

파일 아카이브에는 경로 순회가 없는 `objects/{storedName}` 항목만 있다. PDF·PNG·XLSX·TXT는 표준 라이브러리 기반의 크기 상한, 내부 구조, 능동·외부 콘텐츠 금지, 합성 표식 검사를 통과해야 한다. reset 도구는 DB의 원본명·MIME·크기와 `stored_files.created_at`을 manifest에 대조해 `{base}/{yyyy}/{MM}/{storedName}` 경로를 staging 세대에 만들고 모든 checksum을 다시 검사한다. 각 세대에는 generation, seedVersion, DB/manifest 파일 메타데이터와 SHA-256을 묶은 결정적 `.generation.json`을 두며, 이 표식까지 일치한 뒤에만 `current` 심볼릭 링크를 원자 교체한다.

## 데모 배포 경계

데모는 기존 `compose.yml`을 단독으로 바꾸지 않는다. 모든 데모 명령은 project name `simple-erp-demo`와 `compose.yml`, `compose.demo.yml` 두 파일을 함께 명시한다. overlay는 ARM64를 포함해 검증한 MariaDB 11.8.6 manifest-list digest를 고정하고 DB를 외부 포트에 공개하지 않으며, backend에는 `simple_erp_app`의 live schema DML 권한만 준다. backend는 `DDL_AUTO=validate`로 실행하며 schema·reference 초기화기는 끈다. Caddy TLS 볼륨, DB 볼륨, seed 파일 볼륨도 서로 분리한다. Caddy의 공개 80/443 site는 `SITE_ADDRESS` 도메인만 수락하고 `/actuator/**`를 404로 닫는다. reset·acceptance·healthcheck만 Compose가 host에 publish하지 않는 컨테이너 전용 HTTP `:8080` site의 health endpoint를 사용한다.

파괴 가능한 대상은 다음 literal allowlist로 고정되어 있다.

- 운영 checkout: `/opt/simple-erp-demo`
- Compose project: `simple-erp-demo`
- live DB: `simple_erp_demo`
- candidate DB: `simple_erp_demo_candidate`
- seed 파일 volume: `simple-erp-demo-files`

하나라도 다르면 reset은 삭제나 승격 전에 실패한다. `docker compose down -v`처럼 범위가 넓은 삭제 명령은 사용하지 않는다.

## 운영 설정과 최초 기동

이 데모에는 기존 범용 `scripts/bootstrap-ec2.sh`와 `.github/workflows/deploy.yml`을 사용하지 않는다. 두 경로는 `/opt/simple-erp`와 tag 기반 단일 Compose 배포를 전제로 하므로 `/opt/simple-erp-demo`, 두 Compose 파일, 검증된 digest, reset 계약과 호환되지 않는다. 최초 호스트 준비에는 검토한 commit의 `scripts/demo/bootstrap-demo-ec2.sh`만 사용하고, 인증된 SSH로 전송한 로컬 파일과 원격 파일의 SHA-256이 같은지 확인한 뒤 root로 실행한다. 이 스크립트는 Amazon Linux 2023 ARM64, 서명된 OS 패키지, checksum으로 고정한 Compose·Buildx, Docker 자동 시작, 1GiB swap, root 소유 운영 경로를 검증하며 `ec2-user`를 사실상 root 권한인 docker 그룹에 넣지 않는다.

앱 기동 전에는 `ops/sshd/00-simple-erp-demo-hardening.conf`를 `/etc/ssh/sshd_config.d/`에 `root:root`, mode `644`로 설치한다. OpenSSH는 먼저 읽은 값을 유지하므로 cloud-init 기본값보다 앞서는 `00-` 이름을 보존한다. 먼저 `sshd -t`를 통과시키고 기존 세션을 유지한 채 reload한 다음, 별도 새 세션의 key 로그인과 `sshd -T` 유효값을 확인한다. 이 설정은 root·비밀번호·X11·agent/TCP forwarding을 닫되 `ec2-user`의 public-key 접속은 유지한다. Session Manager 대체 경로를 실제로 검증하기 전에는 보안 그룹의 SSH 규칙을 제거하지 않는다.

운영 호스트는 `!override`를 지원하는 Docker Compose `2.24.4` 이상을 사용한다. `/opt/simple-erp-demo/.env.demo`에는 app DB 비밀번호, JWT, 복구 운영 계정, 공개 데모 자원 상한과 이미지 설정만 둔다. DB root/reset 비밀번호는 여기에 넣지 않고 `/etc/simple-erp-demo/reset.env`에 `root:root`, mode `600`으로 둔다. reset service만 이 파일을 읽으며 backend와 web 컨테이너에는 root 자격 증명을 전달하지 않는다. app DB 비밀번호와 root 비밀번호는 반드시 달라야 한다.

`BACKEND_IMAGE`와 `WEB_IMAGE`에는 같은 배포 commit에서 만든 registry digest(`name[:tag]@sha256:...`)를 명시한다. 로컬 acceptance에서는 Docker image ID(`sha256:...`)도 사용할 수 있다. digest가 함께 있으면 tag는 사람이 버전을 읽기 위한 표시에 불과하고 실제 이미지는 digest로 고정된다. reset은 digest 없는 `sha-*`, `latest`, 임의 tag를 거부하므로 tag의 이동 가능성을 배포 불변식으로 오인하지 않는다. 예제 값은 의도적으로 비워 둔다.

```bash
sudo install -d -o root -g root -m 0755 /etc/simple-erp-demo
sudo install -o root -g root -m 0600 \
  /opt/simple-erp-demo/.env.demo.example /opt/simple-erp-demo/.env.demo
sudo install -o root -g root -m 0600 \
  /opt/simple-erp-demo/ops/systemd/reset.env.example /etc/simple-erp-demo/reset.env
sudoedit /opt/simple-erp-demo/.env.demo
sudoedit /etc/simple-erp-demo/reset.env

sudo install -o root -g root -m 0644 \
  /opt/simple-erp-demo/ops/systemd/simple-erp-demo-reset.service \
  /etc/systemd/system/simple-erp-demo-reset.service
sudo install -o root -g root -m 0644 \
  /opt/simple-erp-demo/ops/systemd/simple-erp-demo-reset.timer \
  /etc/systemd/system/simple-erp-demo-reset.timer
sudo systemctl daemon-reload
```

이미지 게시 workflow의 `demo-image-digests-{commit}` artifact에는 실제 인수 검증을 통과한 backend와 web의 registry digest가 `image-digests.json`으로 기록된다. 운영 호스트에서는 이 값을 `.env.demo`에 쓰기 전에 호스트와 두 이미지의 플랫폼을 먼저 확인한다.

```bash
cd /opt/simple-erp-demo
digest_file=/tmp/image-digests.json

test "$(uname -m)" = "aarch64"
backend_ref="$(jq -er '.backend | select(test("^ghcr\\.io/.+@sha256:[0-9a-f]{64}$"))' "${digest_file}")"
web_ref="$(jq -er '.web | select(test("^ghcr\\.io/.+@sha256:[0-9a-f]{64}$"))' "${digest_file}")"

docker buildx imagetools inspect "${backend_ref}" --format '{{json .Image}}' \
  | jq -e -f scripts/demo/require-linux-arm64.jq > /dev/null
docker buildx imagetools inspect "${web_ref}" --format '{{json .Image}}' \
  | jq -e -f scripts/demo/require-linux-arm64.jq > /dev/null

printf 'BACKEND_IMAGE=%s\nWEB_IMAGE=%s\n' "${backend_ref}" "${web_ref}"
sudoedit /opt/simple-erp-demo/.env.demo
# 위에서 확인한 두 digest를 BACKEND_IMAGE와 WEB_IMAGE에 그대로 기록한다.

# Asia/Seoul 기준 00:00, 06:00, 12:00, 18:00을 먼저 단일 일정 원천으로 연다.
sudo systemctl enable --now simple-erp-demo-reset.timer

# timer의 다음 실행 시각을 상태 파일에 기록하며 최초 generation을 복원한다.
sudo systemctl start simple-erp-demo-reset.service
sudo bash -c 'set -a; source /etc/simple-erp-demo/reset.env; set +a; exec /opt/simple-erp-demo/scripts/demo/acceptance-demo.sh'
systemctl list-timers simple-erp-demo-reset.timer
```

플랫폼 검사, `.env.demo` 갱신, reset, acceptance 순서를 바꾸지 않는다. acceptance는 시작과 종료에 reset을 실행하므로 공유 중인 환경이 아니라 배포 직전 점검에서만 실행한다.

수동 reset도 별도 경로를 만들지 않고 같은 service를 실행한다.

```bash
sudo systemctl start simple-erp-demo-reset.service
sudo journalctl -u simple-erp-demo-reset.service -n 200 --no-pager
```

운영 스크립트는 `flock`을 non-blocking으로 획득한다. 이미 reset이 실행 중이면 두 번째 실행은 exit `75`로 건너뛰며, 애플리케이션 내부 scheduler는 사용하지 않는다.
필수 checkout·`.env.demo`·`reset.env`가 없으면 service의 `ExecStartPre`가 실패하도록 두어 timer가 조용히 skip되지 않게 한다.

## reset과 판정 계약

reset은 안전한 candidate generation과 다음 reset 시각을 확정한 직후 `RESETTING`, `writeLocked=true`를 먼저 원자 공개하고 기존 backend를 중지한다. Caddy는 정적 상태 응답과 초기화 화면을 유지하며, backend 하나를 내린 상태에서 image pin·Compose·파일 volume allowlist와 seed checksum·호환 버전을 확인하고 candidate DB를 import한다. 최초 실행의 빈 파일 volume은 허용된 이름과 Compose label을 확인한 뒤 제한된 초기화 컨테이너가 앱 UID·GID와 setgid mode로 한 번만 준비하며, 이후 실행은 그 소유권 계약이 정확히 유지되는지만 확인한다. 따라서 검사와 검증이 진행되는 동안 들어온 업로드를 포함한 쓰기가 이전 generation에 끼어들 수 없고, 제한된 호스트 메모리에서 기존 backend와 preflight backend가 겹쳐 실행되지 않는다. candidate 전용 임시 DB 계정은 해당 schema의 `SELECT`만 허용하며 preflight 뒤 삭제한다. 같은 backend 이미지가 `DDL_AUTO=validate`로 candidate DB와 staging 파일 세대를 읽어 `/actuator/health/readiness`, 데모 계정 로그인, 대표 조회, 권한 거부와 파일 다운로드의 checksum·Content-Type·Content-Length·Content-Disposition을 검사한다.

backend readiness와 smoke의 기본 제한은 120초다. 에뮬레이션처럼 시작이 느린 검증 환경은 `DEMO_SMOKE_TIMEOUT_SECONDS`를 30~900초 범위에서만 늘릴 수 있으며, ARM64 이미지를 QEMU로 실행하는 게시 gate는 420초를 사용한다. 운영 reset은 별도 설정이 없으면 기본 제한을 유지한다.

공개 상태 파일은 같은 파일시스템의 임시 파일을 `fsync`하고 rename하는 방식으로 다음 순서로 교체된다.

```text
RESETTING → VERIFYING → READY
                    ↘ FAILED
```

`RESETTING`을 공개한 직후 기존 backend를 멈추고 candidate 검증까지 쓰기를 잠그며, `VERIFYING`에서는 canonical seed의 DB 행·marker·파일 외 추가 항목이 없는 strict generation 계약을 확인한다. live DB를 다시 import한 뒤 검증된 파일 generation을 `current`로 원자 승격하며, 새 backend generation이 readiness와 종단 smoke를 통과해야만 `READY`와 새 `generation`, `lastResetAt`을 공개한다. `READY`에서 backend가 재시작될 때는 canonical seed와 함께 DB에 기록된 방문자 파일이 실제 파일과 정확히 대응하는지 검사하고, DB에 없는 고아 파일이나 파일 없는 메타데이터는 거부한다. 모든 enabled 상태는 전이 시각 `stateChangedAt`을 함께 공개한다. 어느 단계든 실패하면 publication이 가능한 경우 `FAILED`, `writeLocked=true`로 닫고, 상태 공개 자체가 실패해도 backend는 중지한다. 이때 실패 지점까지 실제로 생성된 candidate DB·파일만 조사용으로 남기며, 보존 여부와 새 live backend 시작 여부는 journal에 `candidateDb`, `candidateFiles`, `liveBackendStarted`로 명시한다. 해당 컨테이너가 실제로 존재할 때만 로그 capture를 시도하며, 아직 만들지 않았거나 성공 정리에서 이미 제거한 artifact가 보존됐다고 기록하지 않는다. 이전 세대를 성공한 현재 세대로 위장하지 않는다.

artifact 정리는 reset lock 안에서 두 번 실행한다. 시작 시에는 `current`와 최근 조사용 파일 generation 최대 4개, 실패 work 디렉터리 최대 4개, 인식 가능한 `preflight-{UUID}.log`·`backend-{UUID}.log` 최대 20개만 보존해 반복 실패도 용량이 무한히 늘지 않게 한다. gate 통과 뒤 새 실행이 실패한 순간의 최대치는 generation 6개, work 5개, generation별 로그 22개이며 다음 시작 시 다시 5개·4개·20개로 줄어든다. 직전 `FAILED`의 candidate는 최소 다음 실행까지 강제 보존한다. 성공 시에는 아직 `VERIFYING`인 상태와 `current` symlink를 교차 확인해 성공한 새 candidate generation 하나만 남긴다. 직전 정상 generation 전체를 같은 reset에서 삭제하므로 방문자가 만든 DB 행뿐 아니라 업로드 파일 bytes도 함께 사라진다. 현재 성공 work 디렉터리까지 지운 뒤에만 `READY`를 공개한다. SIGKILL 뒤 남을 수 있는 정확한 `.staging-{UUID}` 디렉터리, `.current-{UUID}` symlink와 `.status.json.tmp`·`.preflight.json.tmp` 및 이전 PID형 temp 일반 파일도 다음 pre-reset에서 안전하게 회수한다. 파일 root나 generation의 알 수 없는 이름과 인식 가능한 경로의 타입·symlink 위반은 삭제 전에 전체 정리를 실패시키며, work·log의 알 수 없는 이름은 건드리지 않는다.

reset 초기화가 끝나 failure trap이 설치된 시점부터 pre-prune gate가 열리기 전까지의 candidate·schedule·state publication·image·compose·volume·retention 실패는 새 UUID별 로그를 만들지 않는다. 대신 timestamp·stage·line·exit·candidate·FAILED 상태 공개 여부만 담은 최대 4KiB `retention-failure.log` 하나를 고정 temp, `fsync`, 원자 교체로 갱신한다. 다음 reset 시각을 아직 얻지 못한 초기 실패도 `nextResetAt=null`인 유효한 `FAILED`, `writeLocked=true` 상태로 공개할 수 있다. UUID 출력은 검증을 통과한 뒤에만 candidate로 승격하므로 malformed 출력도 유효한 sentinel candidate로 기록된다. `demo_init` 자체가 실패해 안전한 runtime 경로를 확정하지 못한 경우는 systemd journal을 기준으로 진단한다. gate를 통과한 뒤에는 해당 컨테이너가 실제 시작된 경우에만 generation별 preflight/backend 로그를 남기고, 그 이전 단계 정보는 systemd journal을 기준으로 진단한다. 이 파일 로그와 별개로 db·backend·web·demo-tool 컨테이너 stdout/stderr는 Docker `local` driver의 파일당 10MiB, 최대 3개 회전 계약을 적용하며, Caddy access log 비활성만으로 컨테이너 로그가 제한된다고 간주하지 않는다.

`runtime/`의 상태·작업·로그는 실행 산출물이라 Git에서 제외한다. Caddy와 non-root backend가 읽어야 하는 `runtime/state`만 directory mode `755`, 상태 JSON mode `644`로 만들고, `runtime/work`와 `runtime/logs`는 mode `700`으로 닫는다. 상태 API용 JSON에는 DB·JWT·복구 운영 계정 자격 증명을 쓰지 않으며, 표시가 필요한 두 데모 계정만 포함한다.

## 개인정보와 데모 제한

요청 IP는 네트워크 계층에서 수신될 수 있다. 다만 데모 감사 DB에는 visitor IP를 저장하지 않고, Caddy access log도 활성화하지 않아 원문 IP를 보존하지 않는다. backend 공개 포트가 없고 Caddy의 모든 backend proxy가 외부 `X-Forwarded-For`를 직전 peer 주소로 명시적으로 덮어쓰는 경계에서만 interceptor가 이 헤더를 신뢰한다. 로그인 rate limit 식별자는 backend 프로세스 시작 때 만든 무작위 salt와 요청 주소를 SHA-256으로 해시한 키이며 메모리에만 둔다. 만료된 키는 다음 정리 실행에서 제거되므로 기본 설정에서는 최대 약 2분 보존될 수 있다. 이 키는 방문자 분석이나 장기 추적에 사용하지 않는다.

브라우저의 실제 위치 대신 서울 시청 인근 고정 모의 좌표를 사용한다. Caddy는 정확히 4개인 multipart POST 업로드 경로에만 32MB를 허용하고 나머지 `/api/*` 본문은 1MB로 제한한다. Spring multipart request는 32MB, Spring과 UI의 파일당 상한은 30MiB로 두어 multipart 부가 정보를 수용하면서 사용자 파일 경계는 하나로 유지한다. 자유 입력란과 업로드 파일에 방문자가 실제 개인정보를 넣을 가능성까지 자동 판별할 수는 없다. 화면 고지와 6시간 reset으로 보완하며, 이를 개인정보 수집 방지의 완전한 보장으로 표현하지 않는다.

공개 계정이 디스크·메모리·egress 비용을 무제한 소비하지 못하도록 현재 reset generation의 저장 파일을 계정당 256MiB·16개, 전체 512MiB·32개로 제한한다. 이 상한은 manifest의 `reset_at` 이후 DB 메타데이터를 잠금 상태에서 다시 집계하므로 backend 재시작으로 초기화되지 않는다. 저장 직전에는 실제 파일시스템에 최소 5GiB 또는 전체의 20% 중 큰 값이 남는지도 두 번 확인하고, 조회 실패도 저장을 거부한다. JWT·계정 DB 조회 전 ingress는 분당 IP 300·전체 600, 동시 8개로 제한한다. 인증 조회는 계정 120·전체 180과 동시 4개, 일반 쓰기는 계정 60·전체 90과 동시 4개, 코드 미리보기는 계정 20·전체 30과 동시 2개를 적용한다. 업로드와 저장 파일 다운로드는 전체 동시 2개, 계정당 업로드 1개·다운로드 2개로 제한한다. 요청 수는 분당 일반 업로드 계정 10개·전체 16개, Excel import 계정 2개·전체 2개, 다운로드 계정 20개·전체 30개다. Excel import는 파일당 1MiB·데이터 100행·보수적인 OOXML 압축 해제 상한을 적용하고, 성공 행을 고객사와 영업 명부 합산 계정당 500행·전체 1,000행까지 현재 generation의 감사 로그로 영속 집계한다. Excel export는 6개 공개 도메인 모두 전체 entity 조회 전에 PK만 최대 501개 확인하고 500행 초과를 거절하며, export에 실릴 수 있는 자유 TEXT 입력은 create/update 양쪽 4,000자로 제한한다. 로그인도 IP당 10회와 전체 30회를 원자적으로 함께 차감한다. 다운로드는 추가로 시간당 계정 64MiB·전체 96MiB를 넘으면 외부 전송을 거부한다. 이 값은 2GiB RAM·30GiB root volume과 월간 네트워크 무료 사용량에 여유를 남기기 위한 공개 데모 안전 상한이며 정상 기능의 일반 운영 용량을 뜻하지 않는다.

`READY`에서는 고객사·영업 명부 Excel, 게시판·전자결재·경비의 공통 첨부, Drive 파일 업로드를 모두 허용한다. reset 진입과 동시에 모든 쓰기를 잠그고 DB와 파일 generation 교체가 끝나 `READY`가 된 뒤에만 다시 연다. 성공한 reset은 방문자가 만든 DB 행과 업로드 파일 bytes를 함께 제거한다.

데모에서 첨부 연결이나 Drive 항목을 삭제해도 애플리케이션은 `stored_files` 메타데이터와 파일 본체를 즉시 물리 삭제하지 않는다. 삭제 중 일부만 반영되어 DB와 디스크의 수명이 갈라지지 않도록 물리 삭제는 reset이 단독으로 소유한다. 성공한 reset은 canonical DB를 재import하고 새 canonical 파일 generation을 승격한 뒤 이전 generation 전체를 삭제하므로, 연결 해제된 파일과 방문자 업로드 bytes가 같은 reset에서 제거된다.

쓰기 요청 하나가 내부 반복 작업으로 증폭되지 않도록 일괄 변경은 최대 20건으로 제한한다. 직책·제품 카테고리의 전체 스냅샷 순서 변경은 요청과 서비스 경계에서 최대 50건으로 제한하고, 도메인 총량이 이를 넘으면 전체 엔티티를 읽기 전에 중단한다. 이 상한은 Caddy의 일반 API 1MB·정확한 업로드 경로 32MB, Spring·UI 30MiB 파일 상한, 계정별 쓰기 rate limit과 별개로 적용되는 DB fan-out 경계다.

## 자동 검증

`.github/workflows/demo-seed.yml`은 데모 경로 변경 시 다음 계약을 독립적으로 검사한다.

완성된 `.env.demo`와 host 전용 `DEMO_DB_ROOT_PASSWORD` 환경에서 CI와 같은 정적 계약 검증을 로컬에서도 실행할 수 있다.

```bash
docker compose --project-name simple-erp-demo --env-file .env.demo \
  -f compose.yml -f compose.demo.yml --profile tools config --format json \
  > /tmp/simple-erp-demo-compose.json
python3 scripts/demo/verify_demo_contract.py \
  --compose-config /tmp/simple-erp-demo-compose.json --project-root "$PWD"
```

- Python 3.13에서 seed byte 재현성, manifest의 정확한 키·값 계약, PDF·PNG·XLSX·TXT archive 안전 경계와 능동 콘텐츠 음수 fixture
- `bash -n`, ShellCheck, 6시간 timer 단일성, backend readiness 경로, Caddy 상태 경로·access log 비활성·Caddy·Spring request 32MB와 Spring·UI 파일당 30MiB 업로드 계약, Compose overlay 파싱
- MariaDB `11.8.6` fresh import, `generatedAt`·`timezone`·`sourceDateEpoch`과 DB `generated_at`의 동일 시각 계약, `verify-seed.sql` 0행
- enum·데모 계정·권한·채번 누락뿐 아니라 발급 번호 MAX 불일치, 부서·드라이브 순환, 재직 기간 밖 활동, 주 담당 0건, 결재 단계·경비 항목 0건, 개인정보·DB 파일 메타데이터 변조를 묶어 거부하고 fresh seed 복원 뒤 다시 0행이 되는 회귀 계약
- `@PositiveOrZero` 금액 0과 주말을 제외한 다일 휴가처럼 애플리케이션이 허용하는 값은 verifier도 거부하지 않는 정합성 계약
- app DB 계정의 조회 가능 여부와 DDL 거부
- DB/manifest 파일 메타데이터 staging, 세대별 `.generation.json`, checksum, `current` 원자 승격, 성공 시 이전 generation 삭제, SIGKILL temp 회수, 고정 control-plane failure log, bounded artifact/container log retention과 backend 파일 볼륨 read-write·그룹 권한 계약

### 실제 이미지 인수 검증

`scripts/demo/acceptance-demo.sh`는 배포 대상 이미지와 실제 MariaDB·backend·web을 사용해 `canonical reset → 업무 쓰기 → canonical reset → 삭제 확인`을 한 번에 실행한다. 기존 인수 범위는 고객 생성·수정, 설치완료 전이와 정산완료 직접 등록에서 비동기 설비가 정확히 한 번 생성되는지, 해당 설비의 AS 접수, 게시글, 경비 상신·승인, 모의 위치 출퇴근을 검사한다. 데모 계정 권한과 복구 운영 계정 은닉·보호 계정 삭제 거부, 본인·퇴사자 결재선, 퇴사자 계약 배정, 잘못된 계약 일정, 다른 고객사 담당자 참조도 실패하는지도 확인한다.

격리된 로컬 이미지 환경에서 업로드 기능 복구의 완료 조건도 통과했다. 고객사 Excel, 영업 명부 Excel, 게시판·전자결재·경비가 공유하는 일반 첨부, Drive 파일을 각각 업로드해 조회·다운로드했고, backend 재시작 뒤에도 DB와 파일이 정확히 대응했다. 파일당 정확히 30MiB인 첨부는 성공하고 1byte 초과 첨부는 413으로 거부됐다. 데모 backend 연결은 `TRANSACTION_READ_COMMITTED`로 고정해 manifest 잠금 대기 전에 다른 조회가 있어도 직전 커밋된 업로드 사용량을 quota 판정에 반영하며, 이 격리 수준 변경은 데모 Compose에만 적용된다. 이어 reset을 실행해 acceptance에서 만든 13개 업무·파일 ID의 API 조회가 실패하고 직전 generation 디렉터리와 업로드 bytes가 남지 않는 것을 확인했다.

스크립트는 시작 전에도 reset하고 첫 쓰기 이후 성공·실패와 관계없이 정리 reset을 실행한다. 따라서 공유 데모에서 실행하면 기존 방문자 입력이 즉시 사라진다. 배포 전 점검이나 격리된 로컬 환경에서만 실행하고, 운영 checkout에서는 reset service와 같은 root 전용 자격 증명 환경을 사용한다.

```bash
bash scripts/demo/acceptance-demo.sh
```

workflow는 운영 서비스를 배포하거나 운영 볼륨을 삭제하지 않는다. 배포 전에는 배포 대상 backend/web 이미지로 reset을 두 번 이상 반복하고, 첫 실행에서 만든 데이터를 두 번째 실행이 제거하는지와 두 데모 계정·대표 API·파일 다운로드가 다시 통과하는지 확인한다.
