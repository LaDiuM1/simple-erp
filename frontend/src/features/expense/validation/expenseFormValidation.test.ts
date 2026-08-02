import { describe, expect, it } from 'vitest';
import type { ExpenseItemFormValues } from '@/features/expense/types';
import {
  EXPENSE_ITEM_DESCRIPTION_MAX_LENGTH,
  validateExpenseItems,
} from './expenseFormValidation';

function item(description: string): ExpenseItemFormValues {
  return {
    rowId: 1,
    expenseDate: '2026-08-11',
    category: 'MEAL',
    amount: '10000',
    description,
    receipt: [],
  };
}

describe('validateExpenseItems', () => {
  it('서버의 255자 설명 제한까지 허용한다', () => {
    expect(validateExpenseItems([item('가'.repeat(EXPENSE_ITEM_DESCRIPTION_MAX_LENGTH))]))
      .toBeNull();
  });

  it('서버에서 거부될 256자 설명은 제출 전에 차단한다', () => {
    expect(validateExpenseItems([item('가'.repeat(EXPENSE_ITEM_DESCRIPTION_MAX_LENGTH + 1))]))
      .toContain('255자');
  });
});
