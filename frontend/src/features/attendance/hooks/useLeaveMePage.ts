import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useGetMyLeaveBalanceQuery,
  useGetMyLeavesQuery,
} from '@/features/attendance/api/attendanceApi';
import {
  ATTENDANCE_PATH,
  LEAVE_CREATE_PATH,
} from '@/features/attendance/config/attendancePaths';
import type { Leave } from '@/features/attendance/types';

/**
 * 내 휴가 관리 page hook — 잔여 연차 + 내 신청 목록 query / headerActions 묶음.
 * 휴가 신청은 메뉴 사용자 전원 가능 (BE CAN_READ) — 신청 버튼에 write 권한 게이트를 걸지 않는다.
 */
export function useLeaveMePage() {
  const navigate = useNavigate();
  const leavesQuery = useGetMyLeavesQuery();
  const balanceQuery = useGetMyLeaveBalanceQuery();

  const headerActions: PageHeaderAction[] = [
    { design: 'secondary', label: '내 출퇴근', onClick: () => navigate(ATTENDANCE_PATH) },
    {
      design: 'create',
      label: '휴가 신청',
      onClick: () => navigate(LEAVE_CREATE_PATH),
    },
  ];

  /** 결재 문서가 연결된 행만 결재 상세로 진입 — 미연결 (null) 행은 무동작. */
  const onLeaveRowClick = (leave: Leave) => {
    if (leave.approvalDocumentId != null) {
      navigate(`${MENU_PATH[MENU_CODE.APPROVALS]}/${leave.approvalDocumentId}`);
    }
  };

  return {
    queries: { leaves: leavesQuery, balance: balanceQuery },
    headerActions,
    onLeaveRowClick,
  };
}
