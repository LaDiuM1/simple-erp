import * as React from 'react';
import { useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  useCreateContractPaymentMutation,
  useDeleteContractPaymentMutation,
  useUpdateContractPaymentMutation,
} from '@/features/contract/api/contractApi';
import type { ContractPayment, ContractPaymentRequest } from '@/features/contract/types';

interface FormValues {
  label: string;
  plannedDate: string;
  plannedAmount: string;
  paidDate: string;
  paidAmount: string;
  invoiceDate: string;
  invoiceAmount: string;
  note: string;
}

const EMPTY: FormValues = {
  label: '',
  plannedDate: '',
  plannedAmount: '',
  paidDate: '',
  paidAmount: '',
  invoiceDate: '',
  invoiceAmount: '',
  note: '',
};

interface Props {
  open: boolean;
  onClose: () => void;
  contractId: number;
  /** 수정 모드 — 기존 회차. 미지정 시 등록 모드. */
  payment?: ContractPayment;
}

/** 대금 회차 등록 / 수정 모달 — 수정 모드에서는 회차 삭제도 이 모달에서 수행. */
export default function PaymentFormModal({ open, onClose, contractId, payment }: Props) {
  const isEdit = payment !== undefined;
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const [createMut, { isLoading: isCreating }] = useCreateContractPaymentMutation();
  const [updateMut, { isLoading: isUpdating }] = useUpdateContractPaymentMutation();
  const [deleteMut, { isLoading: isDeleting }] = useDeleteContractPaymentMutation();

  const [values, setValues] = useState<FormValues>(() =>
    payment ? toFormValues(payment) : EMPTY,
  );
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);

  React.useEffect(() => {
    if (!open) return;
    setValues(payment ? toFormValues(payment) : EMPTY);
    setDeleteConfirmOpen(false);
  }, [open, payment]);

  const update = <K extends keyof FormValues>(key: K, v: FormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const isSaving = isCreating || isUpdating;

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    if (values.label.trim() === '') {
      snackbar.error('회차 라벨을 입력해주세요.');
      return;
    }

    const body: ContractPaymentRequest = {
      label: values.label.trim(),
      plannedDate: emptyToNull(values.plannedDate),
      plannedAmount: toAmount(values.plannedAmount),
      paidDate: emptyToNull(values.paidDate),
      paidAmount: toAmount(values.paidAmount),
      invoiceDate: emptyToNull(values.invoiceDate),
      invoiceAmount: toAmount(values.invoiceAmount),
      note: emptyToNull(values.note),
    };

    const promise = isEdit && payment
      ? updateMut({ id: payment.id, contractId, body })
      : createMut({ contractId, body });
    await submit(promise, {
      success: isEdit ? '대금 회차가 수정되었습니다.' : '대금 회차가 등록되었습니다.',
      onSuccess: onClose,
    });
  };

  const handleDelete = async () => {
    if (!payment) return;
    setDeleteConfirmOpen(false);
    await submit(deleteMut({ id: payment.id, contractId }), {
      success: '대금 회차가 삭제되었습니다.',
      onSuccess: onClose,
    });
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{isEdit ? '대금 회차 수정' : '대금 회차 등록'}</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <Stack spacing={2}>
            <TextField
              size="small"
              label="회차 라벨"
              required
              value={values.label}
              onChange={(e) => update('label', e.target.value)}
              placeholder="계약금 / 중도금 / 잔금"
              slotProps={{ htmlInput: { maxLength: 50 } }}
            />
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
              <TextField
                size="small"
                type="date"
                label="입금 예정일"
                value={values.plannedDate}
                onChange={(e) => update('plannedDate', e.target.value)}
                sx={{ flex: 1 }}
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <TextField
                size="small"
                type="number"
                label="예정 금액 (원)"
                value={values.plannedAmount}
                onChange={(e) => update('plannedAmount', e.target.value)}
                sx={{ flex: 1 }}
                slotProps={{ htmlInput: { min: 0 } }}
              />
            </Stack>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
              <TextField
                size="small"
                type="date"
                label="입금일"
                value={values.paidDate}
                onChange={(e) => update('paidDate', e.target.value)}
                sx={{ flex: 1 }}
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <TextField
                size="small"
                type="number"
                label="입금액 (원)"
                value={values.paidAmount}
                onChange={(e) => update('paidAmount', e.target.value)}
                sx={{ flex: 1 }}
                slotProps={{ htmlInput: { min: 0 } }}
              />
            </Stack>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
              <TextField
                size="small"
                type="date"
                label="세금계산서 발행일"
                value={values.invoiceDate}
                onChange={(e) => update('invoiceDate', e.target.value)}
                sx={{ flex: 1 }}
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <TextField
                size="small"
                type="number"
                label="세금계산서 금액 (원)"
                value={values.invoiceAmount}
                onChange={(e) => update('invoiceAmount', e.target.value)}
                sx={{ flex: 1 }}
                slotProps={{ htmlInput: { min: 0 } }}
              />
            </Stack>
            <TextField
              size="small"
              label="메모"
              value={values.note}
              onChange={(e) => update('note', e.target.value)}
              placeholder="지원금 입금 연동 등 특이사항"
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
        title="대금 회차 삭제"
        message={`'${values.label || '이 회차'}' 회차를 삭제하시겠습니까?`}
        confirmLabel={isDeleting ? '삭제 중...' : '삭제'}
        danger
        confirmDisabled={isDeleting}
        onConfirm={handleDelete}
        onCancel={() => setDeleteConfirmOpen(false)}
      />
    </Dialog>
  );
}

function toFormValues(p: ContractPayment): FormValues {
  return {
    label: p.label,
    plannedDate: p.plannedDate ?? '',
    plannedAmount: p.plannedAmount == null ? '' : String(p.plannedAmount),
    paidDate: p.paidDate ?? '',
    paidAmount: p.paidAmount == null ? '' : String(p.paidAmount),
    invoiceDate: p.invoiceDate ?? '',
    invoiceAmount: p.invoiceAmount == null ? '' : String(p.invoiceAmount),
    note: p.note ?? '',
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}

function toAmount(v: string): number | null {
  return v.trim() === '' ? null : Number(v.trim());
}
