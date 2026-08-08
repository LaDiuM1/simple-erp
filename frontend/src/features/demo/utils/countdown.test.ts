import { describe, expect, it } from 'vitest';
import { formatCountdown, millisecondsUntil } from './countdown';

describe('demo countdown', () => {
  it('남은 시간을 0 아래로 내리지 않는다', () => {
    const now = Date.parse('2026-08-02T12:00:00.000Z');
    expect(millisecondsUntil('2026-08-02T11:59:59.000Z', now)).toBe(0);
  });

  it('잘못된 시각과 미지정 시각을 구분 가능한 placeholder로 표현한다', () => {
    expect(millisecondsUntil('invalid', Date.now())).toBeNull();
    expect(formatCountdown(null)).toBe('--:--:--');
  });

  it('24시간을 넘는 시간도 누적 시간으로 표시한다', () => {
    expect(formatCountdown((25 * 3_600 + 61) * 1_000)).toBe('25:01:01');
  });
});
