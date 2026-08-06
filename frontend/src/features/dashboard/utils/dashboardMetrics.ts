/** 비율을 UI 진행률로 쓸 수 있도록 0~100 정수 범위로 정규화한다. */
export function ratioPercent(value: number, total: number): number {
  if (!Number.isFinite(value) || !Number.isFinite(total) || total <= 0) return 0;
  return Math.min(100, Math.max(0, Math.round((value / total) * 100)));
}
