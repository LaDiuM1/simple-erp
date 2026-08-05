import { describe, expect, it } from 'vitest';
import { MAX_MONEY_AMOUNT } from '@/shared/validation/money';
import { validatePaymentAmounts, type PaymentAmountValues } from './paymentValidation';

const values = (overrides: Partial<PaymentAmountValues> = {}): PaymentAmountValues => ({
  plannedAmount: '',
  paidDate: '',
  paidAmount: '',
  invoiceDate: '',
  invoiceAmount: '',
  ...overrides,
});

describe('validatePaymentAmounts', () => {
  it('입금일·입금액과 계산서 발행일·금액을 한 쌍으로 입력한다', () => {
    expect(validatePaymentAmounts(values({ paidDate: '2026-08-01' }))).toContain('입금액');
    expect(validatePaymentAmounts(values({ invoiceAmount: '1000' }))).toContain('발행일');
    expect(validatePaymentAmounts(values({
      paidDate: '2026-08-01',
      paidAmount: '1000',
      invoiceDate: '2026-08-02',
      invoiceAmount: '1000',
    }))).toBeNull();
  });

  it('모든 회차 금액에 공통 상한을 적용한다', () => {
    expect(validatePaymentAmounts(values({ plannedAmount: String(MAX_MONEY_AMOUNT) }))).toBeNull();
    expect(validatePaymentAmounts(values({ plannedAmount: String(MAX_MONEY_AMOUNT + 1) }))).toContain('이하');
  });
});
