import { describe, expect, it } from 'vitest';
import { MAX_MONEY_AMOUNT, hasCompleteDateAmountPair, validateMoneyAmount } from './money';

describe('money validation', () => {
  it('금액 상한 경계를 고정한다', () => {
    expect(validateMoneyAmount(String(MAX_MONEY_AMOUNT), true)).toBeNull();
    expect(validateMoneyAmount(String(MAX_MONEY_AMOUNT + 1), true)).toContain('이하');
    expect(validateMoneyAmount(String(Number.MAX_SAFE_INTEGER + 1), true)).toContain('이하');
  });

  it('날짜와 금액을 한 쌍으로 입력한다', () => {
    expect(hasCompleteDateAmountPair('', '')).toBe(true);
    expect(hasCompleteDateAmountPair('2026-08-01', '1000')).toBe(true);
    expect(hasCompleteDateAmountPair('2026-08-01', '')).toBe(false);
    expect(hasCompleteDateAmountPair('', '1000')).toBe(false);
  });
});
