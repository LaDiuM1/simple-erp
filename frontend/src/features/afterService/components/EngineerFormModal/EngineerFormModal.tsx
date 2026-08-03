import * as React from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import {
  modalStateResetKey,
  useResettableState,
} from '@/shared/hooks/useResettableState';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import EmployeeSelectField from '@/features/employee/components/EmployeeSelectField/EmployeeSelectField';
import {
  useCreateEngineerMutation,
  useUpdateEngineerMutation,
} from '@/features/afterService/api/afterServiceApi';
import {
  ENGINEER_TYPE,
  ENGINEER_TYPE_LABELS,
  type Engineer,
  type EngineerRequest,
  type EngineerType,
} from '@/features/afterService/types';

interface FormValues {
  name: string;
  type: string;
  affiliation: string;
  phone: string;
  active: string;
  employeeId: string;
  employeeName: string;
}

const EMPTY: FormValues = {
  name: '',
  type: ENGINEER_TYPE.OUTSOURCED,
  affiliation: '',
  phone: '',
  active: 'true',
  employeeId: '',
  employeeName: '',
};

interface Props {
  open: boolean;
  onClose: () => void;
  /** 수정 모드 — 기존 엔지니어. 미지정 시 등록 모드. */
  engineer?: Engineer;
}

/**
 * 엔지니어 등록 / 수정 모달. 내부 엔지니어는 재직 직원과 연결하고 외부 엔지니어는 링크를 두지 않는다.
 */
export default function EngineerFormModal({ open, onClose, engineer }: Props) {
  const isEdit = engineer !== undefined;
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const [createMut, { isLoading: isCreating }] = useCreateEngineerMutation();
  const [updateMut, { isLoading: isUpdating }] = useUpdateEngineerMutation();

  const [values, setValues] = useResettableState<FormValues>(
    modalStateResetKey(open, engineer?.id),
    () => (engineer ? toFormValues(engineer) : EMPTY),
  );

  const update = <K extends keyof FormValues>(key: K, v: FormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const isSaving = isCreating || isUpdating;

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    if (values.name.trim() === '') {
      snackbar.error('이름을 입력해주세요.');
      return;
    }
    if (values.type === ENGINEER_TYPE.INTERNAL && values.employeeId === '') {
      snackbar.error('내부 엔지니어로 연결할 직원을 선택해주세요.');
      return;
    }

    const body: EngineerRequest = {
      name: values.name.trim(),
      type: values.type as EngineerType,
      affiliation: emptyToNull(values.affiliation),
      phone: emptyToNull(values.phone),
      employeeId: values.type === ENGINEER_TYPE.INTERNAL
        ? Number(values.employeeId)
        : null,
      active: values.active === 'true',
    };

    const promise = isEdit && engineer
      ? updateMut({ id: engineer.id, body })
      : createMut(body);
    await submit(promise, {
      success: isEdit ? '엔지니어 정보가 수정되었습니다.' : '엔지니어가 등록되었습니다.',
      onSuccess: onClose,
    });
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{isEdit ? '엔지니어 수정' : '엔지니어 등록'}</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <Stack spacing={2}>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
              <TextField
                size="small"
                label="이름"
                required
                value={values.name}
                onChange={(e) => update('name', e.target.value)}
                sx={{ flex: 1 }}
                slotProps={{ htmlInput: { maxLength: 50 } }}
              />
              <TextField
                select
                size="small"
                label="구분"
                required
                value={values.type}
                onChange={(e) => {
                  update('type', e.target.value);
                  if (e.target.value !== ENGINEER_TYPE.INTERNAL) {
                    update('employeeId', '');
                    update('employeeName', '');
                  }
                }}
                sx={{ flex: 1 }}
              >
                {(Object.keys(ENGINEER_TYPE_LABELS) as EngineerType[]).map((t) => (
                  <MenuItem key={t} value={t}>
                    {ENGINEER_TYPE_LABELS[t]}
                  </MenuItem>
                ))}
              </TextField>
            </Stack>
            {values.type === ENGINEER_TYPE.INTERNAL && (
              <EmployeeSelectField
                label="연결 직원"
                required
                value={values.employeeId}
                valueLabel={values.employeeName}
                onChange={(id, name) => {
                  update('employeeId', id);
                  update('employeeName', name);
                }}
                helperText="재직 중인 직원만 내부 엔지니어로 연결할 수 있습니다."
              />
            )}
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
              <TextField
                size="small"
                label="소속"
                value={values.affiliation}
                onChange={(e) => update('affiliation', e.target.value)}
                sx={{ flex: 1 }}
                placeholder="문영테크 / YAWEI 등"
                slotProps={{ htmlInput: { maxLength: 100 } }}
              />
              <TextField
                size="small"
                label="연락처"
                value={values.phone}
                onChange={(e) => update('phone', e.target.value)}
                sx={{ flex: 1 }}
                placeholder="010-1234-5678"
                slotProps={{ htmlInput: { maxLength: 30 } }}
              />
            </Stack>
            <TextField
              select
              size="small"
              label="사용 여부"
              value={values.active}
              onChange={(e) => update('active', e.target.value)}
              helperText="계약 종료된 외주 등은 미사용으로 숨깁니다 (기록은 유지)."
            >
              <MenuItem value="true">사용</MenuItem>
              <MenuItem value="false">미사용</MenuItem>
            </TextField>
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

function toFormValues(e: Engineer): FormValues {
  return {
    name: e.name,
    type: e.type,
    affiliation: e.affiliation ?? '',
    phone: e.phone ?? '',
    active: String(e.active),
    employeeId: e.employeeId == null ? '' : String(e.employeeId),
    employeeName: e.employeeName ?? '',
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
