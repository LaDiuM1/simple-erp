import * as React from 'react';
import { useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useTerminateSalesAssignmentMutation } from '@/features/salesCustomer/api/salesCustomerApi';
import {
  todayIsoDate,
  type SalesAssignment,
} from '@/features/salesCustomer/types';
import { getErrorMessage } from '@/shared/api/error';

interface Props {
  open: boolean;
  onClose: () => void;
  customerId: number;
  assignment: SalesAssignment;
}

export default function AssignmentTerminateModal({ open, onClose, customerId, assignment }: Props) {
  const snackbar = useSnackbar();
  const [terminateMut, { isLoading: isSaving }] = useTerminateSalesAssignmentMutation();

  const [endDate, setEndDate] = useState(todayIsoDate());
  const [reason, setReason] = useState('');

  React.useEffect(() => {
    if (!open) return;
    setEndDate(todayIsoDate());
    setReason('');
  }, [open]);

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    if (endDate === '') {
      snackbar.error('종료일을 선택해주세요.');
      return;
    }
    if (endDate < assignment.startDate) {
      snackbar.error('종료일은 시작일보다 빠를 수 없습니다.');
      return;
    }

    try {
      await terminateMut({
        id: assignment.id,
        customerId,
        body: { endDate, reason: reason.trim() === '' ? null : reason.trim() },
      }).unwrap();
      snackbar.success('배정이 종료되었습니다.');
      onClose();
    } catch (err) {
      snackbar.error(getErrorMessage(err, '종료 처리 중 오류가 발생했습니다.'));
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>영업 담당자 배정 종료</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <Stack spacing={2}>
            <TextField
              size="small"
              label="담당 직원"
              value={assignment.employeeName ?? ''}
              disabled
            />
            <TextField
              size="small"
              type="date"
              label="종료일"
              required
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
              slotProps={{ inputLabel: { shrink: true } }}
              helperText={`배정 시작: ${assignment.startDate}`}
            />
            <TextField
              size="small"
              label="종료 사유"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="예: 이직 / 퇴사 / 일반 변경"
              slotProps={{ htmlInput: { maxLength: 200 } }}
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={isSaving}>
            취소
          </Button>
          <Button type="submit" variant="contained" color="warning" disabled={isSaving}>
            {isSaving ? '처리 중...' : '종료'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
