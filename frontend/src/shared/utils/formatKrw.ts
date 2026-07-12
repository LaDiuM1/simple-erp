/** 원화 금액 표기 공용 포맷 — 목록 / 상세 헤더 / 항목 탭 / 폼 합계가 동일 규칙 사용. */
export function formatKrw(amount: number): string {
  return `${amount.toLocaleString('ko-KR')}원`;
}
