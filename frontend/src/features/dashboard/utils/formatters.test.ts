import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { daysSince, formatRelativeTime, formatShortDate } from './formatters';

describe('formatters', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-07-05T14:00:00'));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('formatRelativeTime — 분 / 시간 / 어제 / 절대 날짜 fallback', () => {
    expect(formatRelativeTime('2026-07-05T13:59:30')).toBe('방금 전');
    expect(formatRelativeTime('2026-07-05T13:30:00')).toBe('30분 전');
    expect(formatRelativeTime('2026-07-05T10:00:00')).toBe('4시간 전');
    expect(formatRelativeTime('2026-07-04T14:00:00')).toBe('어제');
    expect(formatRelativeTime('2026-06-01T14:00:00')).toBe('2026.06.01');
  });

  it('formatShortDate — M/D 표기', () => {
    expect(formatShortDate('2026-06-29')).toBe('6/29');
    expect(formatShortDate('2026-12-01')).toBe('12/1');
  });

  it('daysSince — 경과 일수', () => {
    expect(daysSince('2026-07-05T10:00:00')).toBe(0);
    expect(daysSince('2026-06-28T14:00:00')).toBe(7);
  });
});
