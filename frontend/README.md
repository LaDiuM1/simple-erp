# Simple ERP frontend

React 19, TypeScript, RTK Query, MUI 기반 프론트엔드다. 일반 배포에서는 데모 기능이 기본으로 꺼지고, demo overlay에서만 정적 control-plane 상태를 교체한다.

## 데모 경계

- Caddy가 `/api/v1/demo/status`를 backend보다 먼저 exact-match하고 `/srv/demo/status.json`을 `Cache-Control: no-store`로 반환한다.
- 이미지 기본값인 `public/demo/status.json`은 `enabled: false`다. 이 성공 응답을 확인해야 일반 route를 연다.
- 상태 조회 실패, `RESETTING`, `VERIFYING`, `FAILED`는 로그인 화면을 포함한 전체 route 앞에서 fail-closed한다.
- `generation` 변경은 이전 generation ref 갱신 → RTK Query 캐시 초기화 → `/` replace → 복원 안내 순으로 처리한다.
- 데모 계정 버튼은 아이디와 비밀번호만 채우며 자동 로그인하지 않는다.
- 데모 위치는 status의 합성 좌표만 사용하고, 합성 좌표가 없을 때 실제 GPS로 우회하지 않는다.
- 쓰기 잠금은 공통 페이지 액션, 목록 삭제·엑셀 업로드, 첨부, 드라이브, 근태 진입점에 적용한다. 서버 오류 code도 공통 사용자 안내로 변환한다.

## 검증

```bash
npm run lint
npm test
npm run build
npm run test:e2e
npm audit --audit-level=high
```

Vitest는 status boundary, quick-fill, generation 전환, countdown, 업로드 잠금, 모의 위치, error code를 검증한다. Playwright는 backend 없이 status를 mock해 quick-fill과 reset 중 전체 route 차단을 확인한다.

## dependency audit

2026-08-02 기준 semver 호환 업데이트로 audit 항목을 9건에서 2건으로 줄였다. 남은 2건은 `react-router` 7.18.2의 RSC action 경로 advisory 한 건이 직접·전이 의존성에 중복 집계된 결과다. 이 앱은 `BrowserRouter` 기반 SPA이며 RSC/action endpoint를 사용하지 않아 현재 실행 경로에는 노출되지 않는다. `npm audit fix`가 제안하는 7.11.0 downgrade는 선언한 버전 범위를 벗어나고 최신 v7 변경을 되돌리므로 적용하지 않았다. React Router에서 호환 가능한 수정 버전이 공개되면 재검토한다.
