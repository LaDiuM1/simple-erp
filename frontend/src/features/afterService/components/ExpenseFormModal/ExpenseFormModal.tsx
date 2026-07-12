import * as React from 'react';
import { useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  useCreateServiceExpenseMutation,
  useDeleteServiceExpenseMutation,
  useGetEngineersQuery,
  useUpdateServiceExpenseMutation,
} from '@/features/afterService/api/afterServiceApi';
import {
  EXPENSE_PAYER_TYPE_LABELS,
  SERVICE_EXPENSE_CATEGORY,
  SERVICE_EXPENSE_CATEGORY_LABELS,
  type ExpensePayerType,
  type ServiceExpense,
  type ServiceExpenseCategory,
  type ServiceExpenseRequest,
} from '@/features/afterService/types';

interface FormValues {
  category: string;
  amount: string;
  payerType: string;
  paidDate: string;
  engineerId: string;
  note: string;
}

const EMPTY: FormValues = {
  category: SERVICE_EXPENSE_CATEGORY.DAILY_WAGE,
  amount: '',
  payerType: 'COMPANY',
  paidDate: '',
  engineerId: '',
  note: '',
};

interface Props {
  open: boolean;
  onClose: () => void;
  afterServiceId: number;
  /** 수정 모드 — 기존 경비. 미지정 시 등록 모드. */
  expense?: ServiceExpense;
}

/** 경비 등록 / 수정 모달 — 수정 모드에서는 경비 삭제도 이 모달에서 수행. */
export default function ExpenseFormModal({ open, onClose, afterServiceId, expense }: Props) {
  const isEdit = expense !== undefined;
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const [createMut, { isLoading: isCreating }] = useCreateServiceExpenseMutation();
  const [updateMut, { isLoading: isUpdating }] = useUpdateServiceExpenseMutation();
  const [deleteMut, { isLoading: isDeleting }] = useDeleteServiceExpenseMutation();
  const engineersQuery = useGetEngineersQuery();
  const engineers = (engineersQuery.data ?? []).filter(
    (e) => e.active || (expense?.engineerId != null && e.id === expense.engineerId),
  );

  const [values, setValues] = useState<FormValues>(() => (expense ? toFormValues(expense) : EMPTY));
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);

  React.useEffect(() => {
    if (!open) return;
    setValues(expense ? toFormValues(expense) : EMPTY);
    setDeleteConfirmOpen(false);
  }, [open, expense]);

  const update = <K extends keyof FormValues>(key: K, v: FormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const isSaving = isCreating || isUpdating;

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    if (values.amount.trim() === '' || Number.isNaN(Number(values.amount))) {
      snackbar.error('금액을 입력해주세요.');
      return;
    }

    const body: ServiceExpenseRequest = {
      category: values.category as ServiceExpenseCategory,
      amount: Number(values.amount),
      payerType: values.payerType as ExpensePayerType,
      paidDate: emptyToNull(values.paidDate),
      engineerId: values.engineerId === '' ? null : Number(values.engineerId),
      note: emptyToNull(values.note),
    };

    const promise = isEdit && expense
      ? updateMut({ id: expense.id, afterServiceId, body })
      : createMut({ afterServiceId, body });
    await submit(promise, {
      success: isEdit ? '경비가 수정되었습니다.' : '경비가 등록되었습니다.',
      onSuccess: onClose,
    });
  };

  const handleDelete = async () => {
    if (!expense) return;
    setDeleteConfirmOpen(false);
    await submit(deleteMut({ id: expense.id, afterServiceId }), {
      success: '경비가 삭제되었습니다.',
      onSuccess: onClose,
    });
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{isEdit ? '경비 수정' : '경비 등록'}</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <Stack spacing={2}>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
              <TextField
                select
                size="small"
                label="분류"
                required
                value={values.category}
                onChange={(e) => update('category', e.target.value)}
                sx={{ flex: 1 }}
              >
                {(Object.keys(SERVICE_EXPENSE_CATEGORY_LABELS) as ServiceExpenseCategory[]).map((c) => (
                  <MenuItem key={c} value={c}>
                    {SERVICE_EXPENSE_CATEGORY_LABELS[c]}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                size="small"
                type="number"
                label="금액 (원)"
                required
                value={values.amount}
                onChange={(e) => update('amount', e.target.value)}
                sx={{ flex: 1 }}
                placeholder="0"
                slotProps={{ htmlInput: { min: 0 } }}
              />
            </Stack>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
              <TextField
                select
                size="small"
                label="결제 주체"
                required
                value={values.payerType}
                onChange={(e) => update('payerType', e.target.value)}
                sx={{ flex: 1 }}
              >
                {(Object.keys(EXPENSE_PAYER_TYPE_LABELS) as ExpensePayerType[]).map((p) => (
                  <MenuItem key={p} value={p}>
                    {EXPENSE_PAYER_TYPE_LABELS[p]}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                size="small"
                type="date"
                label="결제일"
                value={values.paidDate}
                onChange={(e) => update('paidDate', e.target.value)}
                sx={{ flex: 1 }}
                slotProps={{ inputLabel: { shrink: true } }}
              />
            </Stack>
            <TextField
              select
              size="small"
              label="관련 엔지니어"
              value={values.engineerId}
              onChange={(e) => update('engineerId', e.target.value)}
              helperText="부품비 등 엔지니어와 무관한 경비는 비워둡니다."
            >
              <MenuItem value="">해당 없음</MenuItem>
              {engineers.map((e) => (
                <MenuItem key={e.id} value={String(e.id)}>
                  {e.affiliation ? `${e.name} (${e.affiliation})` : e.name}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              size="small"
              label="메모"
              value={values.note}
              onChange={(e) => update('note', e.target.value)}
              placeholder="숙소명 / 결제 수단 등"
              slotProps={{ htmlInput: { maxLength: 255 } }}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          {isEdit && (
            <Button
              color="error"
              onClick={() => setDeleteConfirmOpen(true)}
              disabled={isSaving || isDeleting}
              sx={{ marginRight: 'auto' }}
            >
              삭제
            </Button>
          )}
          <Button onClick={onClose} disabled={isSaving || isDeleting}>취소</Button>
          <Button type="submit" variant="contained" disabled={isSaving || isDeleting}>
            {isSaving ? '저장 중...' : isEdit ? '저장' : '등록'}
          </Button>
        </DialogActions>
      </form>

      <ConfirmModal
        isOpen={deleteConfirmOpen}
        title="경비 삭제"
        message="선택한 경비 항목을 삭제하시겠습니까?"
        confirmLabel={isDeleting ? '삭제 중...' : '삭제'}
        danger
        confirmDisabled={isDeleting}
        onConfirm={handleDelete}
        onCancel={() => setDeleteConfirmOpen(false)}
      />
    </Dialog>
  );
}

function toFormValues(e: ServiceExpense): FormValues {
  return {
    category: e.category,
    amount: String(e.amount),
    payerType: e.payerType,
    paidDate: e.paidDate ?? '',
    engineerId: e.engineerId == null ? '' : String(e.engineerId),
    note: e.note ?? '',
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
