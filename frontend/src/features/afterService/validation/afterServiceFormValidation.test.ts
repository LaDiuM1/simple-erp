import { describe, expect, it } from 'vitest';
import {
  EMPTY_AFTER_SERVICE_FORM,
  SERVICE_STATUS,
  WARRANTY_DECISION,
  type AfterServiceFormValues,
} from '@/features/afterService/types';
import { afterServiceValidators } from './afterServiceFormValidation';

const values = (overrides: Partial<AfterServiceFormValues>): AfterServiceFormValues => ({
  ...EMPTY_AFTER_SERVICE_FORM,
  customerId: '1',
  receivedDate: '2026-08-01',
  ...overrides,
});

describe('afterServiceValidators', () => {
  it('유상 AS에는 0원보다 큰 청구액을 요구한다', () => {
    const blank = values({ warrantyDecision: WARRANTY_DECISION.PAID, billingAmount: '' });
    const zero = values({ warrantyDecision: WARRANTY_DECISION.PAID, billingAmount: '0' });
    const positive = values({ warrantyDecision: WARRANTY_DECISION.PAID, billingAmount: '1' });

    expect(afterServiceValidators.billingAmount?.(blank.billingAmount, blank)).toBeTruthy();
    expect(afterServiceValidators.billingAmount?.(zero.billingAmount, zero)).toBeTruthy();
    expect(afterServiceValidators.billingAmount?.(positive.billingAmount, positive)).toBeNull();
  });

  it('완료 상태와 완료일의 조합을 검증한다', () => {
    const missing = values({ status: SERVICE_STATUS.COMPLETED, completedDate: '' });
    const premature = values({ status: SERVICE_STATUS.IN_PROGRESS, completedDate: '2026-08-02' });
    const beforeReceipt = values({
      status: SERVICE_STATUS.COMPLETED,
      completedDate: '2026-07-31',
    });
    const valid = values({ status: SERVICE_STATUS.COMPLETED, completedDate: '2026-08-01' });

    expect(afterServiceValidators.completedDate?.(missing.completedDate, missing)).toBeTruthy();
    expect(afterServiceValidators.completedDate?.(premature.completedDate, premature)).toBeTruthy();
    expect(afterServiceValidators.completedDate?.(beforeReceipt.completedDate, beforeReceipt)).toBeTruthy();
    expect(afterServiceValidators.completedDate?.(valid.completedDate, valid)).toBeNull();
  });
});
