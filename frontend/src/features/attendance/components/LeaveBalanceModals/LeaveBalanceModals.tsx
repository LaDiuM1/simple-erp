import * as React from 'react';
import { useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import TextField from '@mui/material/TextField';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useAdjustLeaveBalanceMutation } from '@/features/attendance/api/attendanceApi';
import type { EmployeeLeaveBalance } from '@/features/attendance/types';

export interface LeaveBalanceModalProps {
  /** 부여 조정 대상 — null 이면 모달 닫힘. */
  adjusting: EmployeeLeaveBalance | null;
  onClose: () => void;
}

/** 잔여 연차 페이지 모달 묶음 — 부여 조정 소형 입력 모달 (drive FolderNameModal 과 동일 톤). */
export default function LeaveBalanceModals({ modal }: { modal: LeaveBalanceModalProps }) {
  return (
    <>
      {modal.adjusting && (
        <GrantedDaysModal balance={modal.adjusting} onClose={modal.onClose} />
      )}
    </>
  );
}

/** 대상이 바뀔 때마다 remount 되어 입력값이 대상의 현재 부여 일수로 초기화된다. */
function GrantedDaysModal({
  balance,
  onClose,
}: {
  balance: EmployeeLeaveBalance;
  onClose: () => void;
}) {
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const [adjustMut, { isLoading: isSaving }] = useAdjustLeaveBalanceMutation();
  const [grantedDays, setGrantedDays] = useState(String(balance.grantedDays));

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    const parsed = Number(grantedDays);
    if (grantedDays.trim() === '' || Number.isNaN(parsed) || parsed < 0) {
      snackbar.error('부여 일수를 0 이상 숫자로 입력해주세요.');
      return;
    }

    await submit(
      adjustMut({
        employeeId: balance.employeeId,
        body: { year: balance.year, grantedDays: parsed },
      }),
      {
        success: '부여 일수가 조정되었습니다.',
        onSuccess: onClose,
      },
    );
  };

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>{`부여 조정 — ${balance.employeeName} (${balance.year}년)`}</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <TextField
            autoFocus
            fullWidth
            size="small"
            type="number"
            label="부여 일수"
            required
            value={grantedDays}
            onChange={(e) => setGrantedDays(e.target.value)}
            helperText={`사용 ${balance.usedDays}일 / 잔여 ${balance.remainingDays}일`}
            slotProps={{ htmlInput: { min: 0, step: 0.5 } }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={isSaving}>취소</Button>
          <Button type="submit" variant="contained" disabled={isSaving}>
            {isSaving ? '저장 중...' : '저장'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
