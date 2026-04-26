import * as React from 'react';
import { useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import EmployeeSelectField from '@/features/employee/components/EmployeeSelectField';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  useCreateSalesAssignmentMutation,
  useUpdateSalesAssignmentMutation,
} from '@/features/salesCustomer/api/salesCustomerApi';
import {
  todayIsoDate,
  type SalesAssignment,
} from '@/features/salesCustomer/types';
import { getErrorMessage } from '@/shared/api/error';

interface FormValues {
  employeeId: string;
  employeeName: string;
  startDate: string;
  primary: boolean;
  reason: string;
}

const EMPTY: FormValues = {
  employeeId: '',
  employeeName: '',
  startDate: '',
  primary: false,
  reason: '',
};

interface Props {
  open: boolean;
  onClose: () => void;
  customerId: number;
  /** 수정 모드 — 기존 배정 데이터. 미지정 시 등록 모드. */
  assignment?: SalesAssignment;
}

export default function AssignmentFormModal({ open, onClose, customerId, assignment }: Props) {
  const isEdit = assignment !== undefined;
  const snackbar = useSnackbar();
  const [createMut, { isLoading: isCreating }] = useCreateSalesAssignmentMutation();
  const [updateMut, { isLoading: isUpdating }] = useUpdateSalesAssignmentMutation();

  const [values, setValues] = useState<FormValues>(() =>
    assignment ? toFormValues(assignment) : { ...EMPTY, startDate: todayIsoDate() },
  );

  React.useEffect(() => {
    if (!open) return;
    setValues(assignment ? toFormValues(assignment) : { ...EMPTY, startDate: todayIsoDate() });
  }, [open, assignment]);

  const update = <K extends keyof FormValues>(key: K, v: FormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const isSaving = isCreating || isUpdating;

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    if (!isEdit && values.employeeId === '') {
      snackbar.error('영업 담당 직원을 선택해주세요.');
      return;
    }
    if (values.startDate === '') {
      snackbar.error('배정 시작일을 입력해주세요.');
      return;
    }

    try {
      if (isEdit && assignment) {
        await updateMut({
          id: assignment.id,
          customerId,
          body: {
            startDate: values.startDate,
            primary: values.primary,
            reason: emptyToNull(values.reason),
          },
        }).unwrap();
        snackbar.success('배정이 수정되었습니다.');
      } else {
        await createMut({
          customerId,
          employeeId: Number(values.employeeId),
          startDate: values.startDate,
          primary: values.primary,
          reason: emptyToNull(values.reason),
        }).unwrap();
        snackbar.success('배정이 등록되었습니다.');
      }
      onClose();
    } catch (err) {
      snackbar.error(getErrorMessage(err, '저장 중 오류가 발생했습니다.'));
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{isEdit ? '영업 담당자 수정' : '영업 담당자 배정'}</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <Stack spacing={2}>
            <EmployeeSelectField
              label="영업 담당 직원"
              required
              value={values.employeeId}
              valueLabel={values.employeeName}
              onChange={(id, name) => {
                update('employeeId', id);
                update('employeeName', name);
              }}
              disabled={isEdit}
              helperText={isEdit ? '담당자 변경은 종료 후 신규 배정으로 진행하세요.' : undefined}
            />
            <TextField
              size="small"
              type="date"
              label="배정 시작일"
              required
              value={values.startDate}
              onChange={(e) => update('startDate', e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
            />
            <FormControlLabel
              control={
                <Checkbox
                  checked={values.primary}
                  onChange={(e) => update('primary', e.target.checked)}
                />
              }
              label="주담당으로 지정 (기존 주담당이 있으면 자동 해제)"
            />
            <TextField
              size="small"
              label="배정 / 변경 사유"
              value={values.reason}
              onChange={(e) => update('reason', e.target.value)}
              slotProps={{ htmlInput: { maxLength: 200 } }}
              placeholder="예: 영업1팀 신규 고객 배정"
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={isSaving}>
            취소
          </Button>
          <Button type="submit" variant="contained" disabled={isSaving}>
            {isSaving ? '저장 중...' : isEdit ? '저장' : '배정'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

function toFormValues(a: SalesAssignment): FormValues {
  return {
    employeeId: String(a.employeeId),
    employeeName: a.employeeName ?? '',
    startDate: a.startDate,
    primary: a.primary,
    reason: a.reason ?? '',
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
