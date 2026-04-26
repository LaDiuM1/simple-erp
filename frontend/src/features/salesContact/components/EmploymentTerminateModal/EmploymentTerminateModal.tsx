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
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useTerminateSalesContactEmploymentMutation } from '@/features/salesContact/api/salesContactApi';
import {
  DEPARTURE_TYPE_OPTIONS,
  type DepartureType,
  type SalesContactEmployment,
} from '@/features/salesContact/types';
import type { ApiError } from '@/shared/types/api';

interface Props {
  open: boolean;
  onClose: () => void;
  contactId: number;
  employment: SalesContactEmployment;
}

export default function EmploymentTerminateModal({ open, onClose, contactId, employment }: Props) {
  const snackbar = useSnackbar();
  const [terminateMut, { isLoading: isSaving }] = useTerminateSalesContactEmploymentMutation();

  const [endDate, setEndDate] = useState(todayIsoDate());
  const [departureType, setDepartureType] = useState<DepartureType>('JOB_CHANGE');
  const [departureNote, setDepartureNote] = useState('');

  React.useEffect(() => {
    if (!open) return;
    setEndDate(todayIsoDate());
    setDepartureType('JOB_CHANGE');
    setDepartureNote('');
  }, [open]);

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    if (endDate === '') {
      snackbar.error('종료일을 선택해주세요.');
      return;
    }
    if (endDate < employment.startDate) {
      snackbar.error('종료일은 시작일보다 빠를 수 없습니다.');
      return;
    }

    try {
      await terminateMut({
        id: employment.id,
        contactId,
        customerId: employment.customerId,
        body: {
          endDate,
          departureType,
          departureNote: departureNote.trim() === '' ? null : departureNote.trim(),
        },
      }).unwrap();
      snackbar.success('재직이 종료되었습니다.');
      onClose();
    } catch (err) {
      snackbar.error((err as ApiError)?.message ?? '종료 처리 중 오류가 발생했습니다.');
    }
  };

  const companyLabel = employment.customerName ?? employment.externalCompanyName ?? '-';

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>재직 종료</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <Stack spacing={2}>
            <TextField size="small" label="회사" value={companyLabel} disabled />
            <TextField
              size="small"
              type="date"
              label="종료일"
              required
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
              helperText={`재직 시작: ${employment.startDate}`}
            />
            <TextField
              select
              size="small"
              label="종료 분류"
              required
              value={departureType}
              onChange={(e) => setDepartureType(e.target.value as DepartureType)}
            >
              {DEPARTURE_TYPE_OPTIONS.map((opt) => (
                <MenuItem key={opt.value} value={opt.value}>
                  {opt.label}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              size="small"
              label="종료 사유 / 메모"
              value={departureNote}
              onChange={(e) => setDepartureNote(e.target.value)}
              placeholder="예: 다른 회사로 이직 / 정년 퇴직"
              slotProps={{ htmlInput: { maxLength: 200 } }}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={isSaving}>취소</Button>
          <Button type="submit" variant="contained" color="warning" disabled={isSaving}>
            {isSaving ? '처리 중...' : '종료'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

function todayIsoDate(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
