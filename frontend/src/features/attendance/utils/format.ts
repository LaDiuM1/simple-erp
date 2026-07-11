const WEEKDAY_LABELS = ['일', '월', '화', '수', '목', '금', '토'] as const;

/** ISO LocalDateTime ('2026-07-10T09:01:23') → 'HH:mm'. null 은 그대로 통과 (Muted 처리 위임). */
export function formatTime(dateTime: string | null): string | null {
  if (!dateTime) return null;
  return dateTime.slice(11, 16);
}

/** 'YYYY-MM-DD' → 'YYYY-MM-DD (요일)'. */
export function formatDateWithDay(date: string): string {
  const [year, month, day] = date.split('-').map(Number);
  const weekday = new Date(year, month - 1, day).getDay();
  return `${date} (${WEEKDAY_LABELS[weekday]})`;
}

/** 휴가 기간 표시 — 하루면 시작일만, 기간이면 '시작 ~ 종료'. */
export function formatLeavePeriod(startDate: string, endDate: string): string {
  return startDate === endDate ? startDate : `${startDate} ~ ${endDate}`;
}
