import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { formatRelativeTime, formatTodayLong } from './formatters';

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

  it('formatTodayLong — 한국어 긴 날짜', () => {
    expect(formatTodayLong(new Date('2026-07-05T14:00:00'))).toContain('2026');
    expect(formatTodayLong(new Date('2026-07-05T14:00:00'))).toContain('7월');
  });
});
