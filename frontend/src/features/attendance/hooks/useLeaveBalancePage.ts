import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useGetLeaveBalancesQuery } from '@/features/attendance/api/attendanceApi';
import { LEAVE_STATUS_PATH } from '@/features/attendance/config/attendancePaths';
import { yearFilterOptions } from '@/features/attendance/utils/periodOptions';
import type { EmployeeLeaveBalance } from '@/features/attendance/types';
import type { LeaveBalanceModalProps } from '@/features/attendance/components/LeaveBalanceModals/LeaveBalanceModals';

/**
 * 전 직원 잔여 연차 관리 page hook — 연도 선택 + 잔여 목록 query + 부여 조정 모달 state 묶음.
 * headerActions 는 페이지의 write 권한 게이트 안에서 렌더 — hook 은 권한을 재확인하지 않는다.
 * Hook 은 JSX 반환하지 않음 (CLAUDE.md) — 모달은 LeaveBalanceModals 컴포넌트가 명시 렌더.
 */
export function useLeaveBalancePage() {
  const navigate = useNavigate();

  const currentYear = new Date().getFullYear();
  const [year, setYear] = useState(currentYear);
  const balancesQuery = useGetLeaveBalancesQuery({ year });

  const [adjusting, setAdjusting] = useState<EmployeeLeaveBalance | null>(null);

  const headerActions: PageHeaderAction[] = [
    { design: 'secondary', label: '휴가 현황', onClick: () => navigate(LEAVE_STATUS_PATH) },
  ];

  const modal: LeaveBalanceModalProps = {
    adjusting,
    onClose: () => setAdjusting(null),
  };

  return {
    queries: { balances: balancesQuery },
    headerActions,
    yearFilter: {
      year,
      options: yearFilterOptions(),
      onChange: (v: number | null) => setYear(v ?? currentYear),
    },
    onAdjust: (balance: EmployeeLeaveBalance) => setAdjusting(balance),
    modal,
  };
}
