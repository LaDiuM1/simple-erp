import { describe, expect, it } from 'vitest';
import { ratioPercent } from './dashboardMetrics';

describe('ratioPercent', () => {
  it('비율을 반올림한 백분율로 반환한다', () => {
    expect(ratioPercent(7, 12)).toBe(58);
  });

  it('분모가 없거나 잘못된 입력이면 0으로 처리한다', () => {
    expect(ratioPercent(10, 0)).toBe(0);
    expect(ratioPercent(Number.NaN, 10)).toBe(0);
  });

  it('진행률 범위를 0에서 100 사이로 제한한다', () => {
    expect(ratioPercent(-10, 100)).toBe(0);
    expect(ratioPercent(120, 100)).toBe(100);
  });
});
