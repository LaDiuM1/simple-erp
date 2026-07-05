/**
 * "방금 전 / N분 전 / N시간 전 / 어제 / N일 전 / yyyy.mm.dd" 형태의 상대 시간.
 * 7일 이전은 절대 날짜로 fallback.
 */
export function formatRelativeTime(iso: string): string {
  const target = new Date(iso);
  const diffMs = Date.now() - target.getTime();
  const diffSec = Math.floor(diffMs / 1000);
  if (diffSec < 60) return '방금 전';
  const diffMin = Math.floor(diffSec / 60);
  if (diffMin < 60) return `${diffMin}분 전`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour}시간 전`;
  const diffDay = Math.floor(diffHour / 24);
  if (diffDay === 1) return '어제';
  if (diffDay < 7) return `${diffDay}일 전`;
  return `${target.getFullYear()}.${pad(target.getMonth() + 1)}.${pad(target.getDate())}`;
}

/** "M/D" 형태의 짧은 날짜 — 차트 x축 / 팔로업 마지막 접촉일 등 컴팩트 표기. */
export function formatShortDate(iso: string): string {
  const d = new Date(iso);
  return `${d.getMonth() + 1}/${d.getDate()}`;
}

/** 주어진 시점부터 오늘까지 경과 일수. */
export function daysSince(iso: string): number {
  return Math.floor((Date.now() - new Date(iso).getTime()) / 86_400_000);
}

export function formatTodayLong(date: Date): string {
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  });
}

function pad(n: number): string {
  return n.toString().padStart(2, '0');
}
