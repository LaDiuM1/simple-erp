import type { ValidatorMap } from '@/shared/hooks/useFieldValidation';
import type { ExpenseFormValues, ExpenseItemFormValues } from '@/features/expense/types';

/** BE ExpenseCreateRequest 의 @NotBlank title 미러 — 즉시 피드백용. */
export const expenseValidators: ValidatorMap<ExpenseFormValues> = {
  title: (v) => (v.trim() === '' ? '제목을 입력해주세요.' : null),
};

/**
 * 경비 항목 행 단위 검증 — BE 의 EMPTY_ITEMS / @NotNull expenseDate / @Positive amount 미러.
 * 동적 행이라 필드 단위 touched 대신 submit 시 일괄 확인, 첫 위반 메시지를 반환 (통과 시 null).
 */
export function validateExpenseItems(items: ExpenseItemFormValues[]): string | null {
  if (items.length === 0) return '경비 항목을 1개 이상 추가해주세요.';
  for (const [index, item] of items.entries()) {
    const no = index + 1;
    if (item.expenseDate === '') return `${no}번째 항목의 지출 일자를 선택해주세요.`;
    const amount = Number(item.amount);
    if (item.amount.trim() === '' || Number.isNaN(amount) || amount <= 0) {
      return `${no}번째 항목의 금액을 0보다 크게 입력해주세요.`;
    }
  }
  return null;
}
