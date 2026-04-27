import * as React from 'react';
import { useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import CustomerSelectField from '@/features/customer/components/CustomerSelectField';
import ModeChoiceSection from '@/shared/ui/ModeChoiceSection';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  useCreateSalesContactEmploymentMutation,
  useUpdateSalesContactEmploymentMutation,
} from '@/features/salesContact/api/salesContactApi';
import type { SalesContactEmployment } from '@/features/salesContact/types';
import type { ApiError } from '@/shared/types/api';

type CompanyMode = 'lookup' | 'manual';

interface FormValues {
  companyMode: CompanyMode;
  customerId: string;
  customerName: string;
  externalCompanyName: string;
  position: string;
  department: string;
  startDate: string;
}

const EMPTY: FormValues = {
  companyMode: 'lookup',
  customerId: '',
  customerName: '',
  externalCompanyName: '',
  position: '',
  department: '',
  startDate: '',
};

interface Props {
  open: boolean;
  onClose: () => void;
  contactId: number;
  /** 수정 모드 — 기존 재직. 미지정 시 등록 모드. */
  employment?: SalesContactEmployment;
}

export default function EmploymentFormModal({ open, onClose, contactId, employment }: Props) {
  const isEdit = employment !== undefined;
  const snackbar = useSnackbar();
  const [createMut, { isLoading: isCreating }] = useCreateSalesContactEmploymentMutation();
  const [updateMut, { isLoading: isUpdating }] = useUpdateSalesContactEmploymentMutation();

  const [values, setValues] = useState<FormValues>(() =>
    employment ? toFormValues(employment) : { ...EMPTY, startDate: todayIsoDate() },
  );

  React.useEffect(() => {
    if (!open) return;
    setValues(employment ? toFormValues(employment) : { ...EMPTY, startDate: todayIsoDate() });
  }, [open, employment]);

  const update = <K extends keyof FormValues>(key: K, v: FormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const switchCompanyMode = (mode: CompanyMode) => {
    setValues((prev) =>
      mode === 'lookup'
        ? { ...prev, companyMode: mode, externalCompanyName: '' }
        : { ...prev, companyMode: mode, customerId: '', customerName: '' },
    );
  };

  const isSaving = isCreating || isUpdating;

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    if (values.startDate === '') {
      snackbar.error('재직 시작일을 선택해주세요.');
      return;
    }
    if (values.companyMode === 'lookup' && values.customerId === '') {
      snackbar.error('관리중 고객사를 선택해주세요.');
      return;
    }
    if (values.companyMode === 'manual' && values.externalCompanyName.trim() === '') {
      snackbar.error('외부 고객사명을 입력해주세요.');
      return;
    }

    const isLookup = values.companyMode === 'lookup';
    const payload = {
      customerId: isLookup && values.customerId !== '' ? Number(values.customerId) : null,
      externalCompanyName: isLookup ? null : values.externalCompanyName.trim(),
      position: emptyToNull(values.position),
      department: emptyToNull(values.department),
      startDate: values.startDate,
    };

    try {
      if (isEdit && employment) {
        await updateMut({
          id: employment.id,
          contactId,
          customerId: employment.customerId,
          body: payload,
        }).unwrap();
        snackbar.success('재직 이력이 수정되었습니다.');
      } else {
        await createMut({ contactId, body: payload }).unwrap();
        snackbar.success('재직이 등록되었습니다.');
      }
      onClose();
    } catch (err) {
      snackbar.error((err as ApiError)?.message ?? '저장 중 오류가 발생했습니다.');
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{isEdit ? '재직 정보 수정' : '재직 등록'}</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <Stack spacing={2}>
            <ModeChoiceSection
              title="재직 회사"
              mode={values.companyMode}
              onModeChange={switchCompanyMode}
              options={[
                { value: 'lookup', label: '관리중 고객사 검색' },
                { value: 'manual', label: '직접 입력' },
              ]}
            >
              {values.companyMode === 'lookup' ? (
                <CustomerSelectField
                  label="관리중 고객사명"
                  required
                  value={values.customerId}
                  valueLabel={values.customerName}
                  onChange={(id, name) => {
                    update('customerId', id);
                    update('customerName', name);
                  }}
                />
              ) : (
                <TextField
                  fullWidth
                  size="small"
                  label="외부 고객사명"
                  required
                  value={values.externalCompanyName}
                  onChange={(e) => update('externalCompanyName', e.target.value)}
                  placeholder="미관리 고객사 직접 입력"
                  slotProps={{ htmlInput: { maxLength: 200 } }}
                />
              )}
            </ModeChoiceSection>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
              <TextField
                size="small"
                label="직책"
                value={values.position}
                onChange={(e) => update('position', e.target.value)}
                sx={{ flex: 1 }}
                slotProps={{ htmlInput: { maxLength: 100 } }}
              />
              <TextField
                size="small"
                label="부서"
                value={values.department}
                onChange={(e) => update('department', e.target.value)}
                sx={{ flex: 1 }}
                slotProps={{ htmlInput: { maxLength: 100 } }}
              />
            </Stack>
            <TextField
              size="small"
              type="date"
              label="재직 시작일"
              required
              value={values.startDate}
              onChange={(e) => update('startDate', e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={isSaving}>취소</Button>
          <Button type="submit" variant="contained" disabled={isSaving}>
            {isSaving ? '저장 중...' : isEdit ? '저장' : '등록'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

function toFormValues(e: SalesContactEmployment): FormValues {
  const hasLookup = e.customerId != null;
  return {
    companyMode: hasLookup ? 'lookup' : 'manual',
    customerId: e.customerId == null ? '' : String(e.customerId),
    customerName: e.customerName ?? '',
    externalCompanyName: e.externalCompanyName ?? '',
    position: e.position ?? '',
    department: e.department ?? '',
    startDate: e.startDate,
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}

function todayIsoDate(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
