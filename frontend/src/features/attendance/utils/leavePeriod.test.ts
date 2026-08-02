import { describe, expect, it } from 'vitest';
import {
  MAX_LEAVE_PERIOD_SPAN_DAYS,
  validateLeavePeriod,
} from './leavePeriod';

describe('validateLeavePeriod', () => {
  it('서버 계약과 같은 365일 상한을 사용한다', () => {
    expect(MAX_LEAVE_PERIOD_SPAN_DAYS).toBe(365);
  });

  it('연도를 넘는 휴가 기간을 제출 전에 거부한다', () => {
    expect(validateLeavePeriod('2026-12-31', '2027-01-01')).toBe('DIFFERENT_YEAR');
  });

  it('같은 연도의 정상 기간은 허용한다', () => {
    expect(validateLeavePeriod('2026-08-10', '2026-08-14')).toBeNull();
  });

  it('시작·종료일을 포함한 365일은 허용한다', () => {
    expect(validateLeavePeriod('2028-01-01', '2028-12-30')).toBeNull();
  });

  it('윤년 전체 366일은 상한을 넘어 거절한다', () => {
    expect(validateLeavePeriod('2028-01-01', '2028-12-31')).toBe('TOO_LONG');
  });

  it('존재하지 않는 날짜를 거부한다', () => {
    expect(validateLeavePeriod('2026-02-30', '2026-03-01')).toBe('INVALID');
  });
});
