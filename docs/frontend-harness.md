# FE 하네스 지침

에이전트용 최소 지침. 원칙 하나: **작업 결과를 스스로, 빠르게, 결정론적으로 검증한다.**
컨벤션·아키텍처의 단일 진실 소스는 루트 `CLAUDE.md` — 충돌 시 그쪽이 이긴다.

## 검증 명령 (작업 후 필수, 통과 전 커밋 금지)

| 대상 | 명령 | 비고 |
|---|---|---|
| FE 타입 | `npm run typecheck` | 가장 빠른 루프 |
| FE 테스트 | `npm run test` | vitest |
| FE 린트 | `npm run lint` | 기존 에러 53건 존재 (별도 정리 전) — 신규 파일만 클린 유지 |
| FE 빌드 | `npm run build` | 최종 확인용 |
| BE | `./gradlew test` | JUnit + Mockito |

출력 규칙: 성공은 조용히, 실패는 시끄럽게 — 성공 시 tail 요약만, 실패 시 에러 본문을 읽는다.

## 스택 (확정 — 변경 제안 시 근거 필요)

TS strict · React 19 + Vite 8 SPA · npm · MUI v7 `styled()` + `.styles.ts` · React Router v7 · RTK Query(서버) + createSlice(클라이언트) · ESLint · vitest + testing-library + msw

## 테스트 컨벤션

- 테스트 파일은 대상 옆 `*.test.ts(x)`, 공용 헬퍼·msw 는 `src/test/`
- styled 컴포넌트 렌더는 `renderWithTheme` 사용
- msw 핸들러는 `ApiResponse`/`PageResponse` 래핑 규격 준수 (`src/test/msw/handlers.ts`)
- exemplar: `formatters.test.ts`(순수) · `StatTile.test.tsx`(컴포넌트) · `msw.test.ts`(목킹)

## 작업 흐름

1. 요구 불명확 → 진행 말고 질문
2. 작업 단위 = 브랜치 + PR (master 직접 push 금지)
3. 구현 → 검증 명령 전부 통과 → 커밋 (심플 컨벤션: 제목 + 최소 단위 변경 bullet)
4. 신규 도메인은 기존 도메인(employee / customer)을 exemplar 로 — 새 패턴 발명 금지
5. 규격 밖 요구는 페이지에 넣지 말고 공용 레이어(Generic*/shared) 확장

## 남은 로드맵

- [ ] Playwright + MCP (시각 검증 — 렌더 변경을 스크린샷으로 닫기)
- [ ] CI 게이트에 typecheck + lint + test 추가 (현재 Docker 빌드만)
- [ ] 기존 lint 에러 53건 정리
- [ ] 그룹웨어 도메인: storage → approval → expense → attendance → board
