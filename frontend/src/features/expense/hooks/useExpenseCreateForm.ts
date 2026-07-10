import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_PATH } from '@/shared/config/menuConfig';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useFieldValidation, type FieldValidation } from '@/shared/hooks/useFieldValidation';
import { useToggle } from '@/shared/hooks/useToggle';
import { useFormState } from '@/shared/ui/GenericForm/useFormState';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import type { AttachedFile } from '@/shared/ui/FileAttachField';
import { useCreateExpenseMutation } from '@/features/expense/api/expenseApi';
import {
  createEmptyExpenseItem,
  emptyExpenseForm,
  expenseFormToCreateRequest,
  type ExpenseFormValues,
  type ExpenseItemFormValues,
} from '@/features/expense/types';
import {
  expenseValidators,
  validateExpenseItems,
} from '@/features/expense/validation/expenseFormValidation';

export interface ExpenseCreateFormState {
  values: ExpenseFormValues;
  update: <K extends keyof ExpenseFormValues>(key: K, v: ExpenseFormValues[K]) => void;
  validation: FieldValidation<ExpenseFormValues>;
  updateItem: <K extends keyof ExpenseItemFormValues>(
    rowId: number,
    key: K,
    v: ExpenseItemFormValues[K],
  ) => void;
  /** 행 영수증 갱신 — FileAttachField 함수형 onChange 를 해당 행의 receipt 에 prev 기반 반영. */
  updateItemReceipt: (rowId: number, apply: (prev: AttachedFile[]) => AttachedFile[]) => void;
  addItem: () => void;
  removeItem: (rowId: number) => void;
  /** 항목 금액 합 — 입력 중 실시간 갱신 (숫자 미완성 행은 0 취급). */
  totalAmount: number;
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

/**
 * 경비 등록 form-state hook — 생성 = 즉시 상신이라 submit 전 ConfirmModal 로 상신 의사 확인.
 * 항목은 rowId 기반 동적 행 — 검증은 제목 (필드 단위) + 항목/결재선 (submit 시 snackbar) 이중 구조.
 */
export function useExpenseCreateForm(): ExpenseCreateFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const submit = useApiSubmit();

  const { values, updateField: update, setAll } = useFormState<ExpenseFormValues>(emptyExpenseForm);
  const [confirmOpen, confirm] = useToggle();
  const [createExpense, { isLoading: isSaving }] = useCreateExpenseMutation();

  const validation = useFieldValidation(values, expenseValidators);

  const totalAmount = values.items.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);

  // 항목 조작은 모두 prev 기반 — 영수증 업로드 완료 콜백 등 비동기 시점의 stale 스냅샷이
  // 업로드 중 수정한 다른 행 입력을 덮어쓰지 않도록 한다.
  const updateItem = <K extends keyof ExpenseItemFormValues>(
    rowId: number,
    key: K,
    v: ExpenseItemFormValues[K],
  ) => {
    setAll((prev) => ({
      ...prev,
      items: prev.items.map((item) => (item.rowId === rowId ? { ...item, [key]: v } : item)),
    }));
  };

  const updateItemReceipt = (rowId: number, apply: (prev: AttachedFile[]) => AttachedFile[]) => {
    setAll((prev) => ({
      ...prev,
      items: prev.items.map((item) =>
        item.rowId === rowId ? { ...item, receipt: apply(item.receipt) } : item,
      ),
    }));
  };

  const addItem = () => {
    setAll((prev) => ({
      ...prev,
      items: [...prev.items, createEmptyExpenseItem(nextRowId(prev.items))],
    }));
  };

  const removeItem = (rowId: number) => {
    setAll((prev) => ({
      ...prev,
      items: prev.items.filter((item) => item.rowId !== rowId),
    }));
  };

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    if (!validation.validateAll()) {
      snackbar.error('입력값을 확인해주세요.');
      return;
    }
    const itemError = validateExpenseItems(values.items);
    if (itemError) {
      snackbar.error(itemError);
      return;
    }
    if (values.approvalLine.length === 0) {
      snackbar.error('결재자를 1명 이상 추가해주세요.');
      return;
    }
    confirm.on();
  };

  const handleConfirmedSubmit = async () => {
    confirm.off();
    await submit(createExpense(expenseFormToCreateRequest(values)), {
      success: '경비 청구가 상신되었습니다.',
      navigateTo: MENU_PATH.EXPENSES,
    });
  };

  return {
    values,
    update,
    validation,
    updateItem,
    updateItemReceipt,
    addItem,
    removeItem,
    totalAmount,
    isSaving,
    confirmOpen,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: confirm.off,
    handleCancel: () => navigate(MENU_PATH.EXPENSES),
  };
}

/** 다음 행 식별자 — 현재 행들의 최대 rowId + 1 (삭제 후 재추가에도 충돌 없음). */
function nextRowId(items: ExpenseItemFormValues[]): number {
  return items.reduce((max, item) => Math.max(max, item.rowId), -1) + 1;
}
