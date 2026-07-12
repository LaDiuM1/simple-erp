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
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
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
}

const EMPTY: FormValues = {
  name: '',
  type: ENGINEER_TYPE.OUTSOURCED,
  affiliation: '',
  phone: '',
  active: 'true',
};

interface Props {
  open: boolean;
  onClose: () => void;
  /** 수정 모드 — 기존 엔지니어. 미지정 시 등록 모드. */
  engineer?: Engineer;
}

/**
 * 엔지니어 등록 / 수정 모달. 내부 직원 링크 (employeeId) 는 현재 화면에서 받지 않는다 —
 * 실무 식별은 이름 / 소속으로 충분하고, 직원 연동 필요가 생기면 EmployeeSelectField 를 추가한다.
 */
export default function EngineerFormModal({ open, onClose, engineer }: Props) {
  const isEdit = engineer !== undefined;
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const [createMut, { isLoading: isCreating }] = useCreateEngineerMutation();
  const [updateMut, { isLoading: isUpdating }] = useUpdateEngineerMutation();

  const [values, setValues] = useState<FormValues>(() =>
    engineer ? toFormValues(engineer) : EMPTY,
  );

  React.useEffect(() => {
    if (!open) return;
    setValues(engineer ? toFormValues(engineer) : EMPTY);
  }, [open, engineer]);

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

    const body: EngineerRequest = {
      name: values.name.trim(),
      type: values.type as EngineerType,
      affiliation: emptyToNull(values.affiliation),
      phone: emptyToNull(values.phone),
      employeeId: engineer?.employeeId ?? null,
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
                onChange={(e) => update('type', e.target.value)}
                sx={{ flex: 1 }}
              >
                {(Object.keys(ENGINEER_TYPE_LABELS) as EngineerType[]).map((t) => (
                  <MenuItem key={t} value={t}>
                    {ENGINEER_TYPE_LABELS[t]}
                  </MenuItem>
                ))}
              </TextField>
            </Stack>
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
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
