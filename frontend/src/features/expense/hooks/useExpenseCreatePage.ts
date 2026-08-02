import { MENU_CODE } from '@/shared/config/menuConfig';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { EXPENSE_CREATE_FORM_ID } from '@/features/expense/components/ExpenseCreateForm/ExpenseCreateForm';
import { useExpenseCreateForm, type ExpenseCreateFormState } from './useExpenseCreateForm';

/**
 * 경비 등록 page hook — form-state hook + headerActions 묶음 (outer fetch 없음).
 * 등록 버튼은 formId 포털로 Body 의 form submit 에 연결.
 */
export function useExpenseCreatePage(): {
  form: ExpenseCreateFormState;
  headerActions: PageHeaderAction[];
} {
  const form = useExpenseCreateForm();

  const headerActions: PageHeaderAction[] = [
    { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
    {
      design: 'create',
      formId: EXPENSE_CREATE_FORM_ID,
      loading: form.isSaving,
      menuCode: MENU_CODE.EXPENSES,
      permission: 'read',
    },
  ];

  return { form, headerActions };
}
