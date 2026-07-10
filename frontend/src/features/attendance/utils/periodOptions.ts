/** 근태 조회용 년 / 월 select 옵션 — 내 출퇴근 / 관리자 현황 필터 공용. */

export function yearFilterOptions(count = 3): { value: number; label: string }[] {
  const currentYear = new Date().getFullYear();
  return Array.from({ length: count }, (_, i) => ({
    value: currentYear - i,
    label: `${currentYear - i}년`,
  }));
}

export const MONTH_FILTER_OPTIONS: { value: number; label: string }[] = Array.from(
  { length: 12 },
  (_, i) => ({ value: i + 1, label: `${i + 1}월` }),
);
