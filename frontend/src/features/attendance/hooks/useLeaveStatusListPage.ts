import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useGetLeavesQuery } from '@/features/attendance/api/attendanceApi';
import {
  ATTENDANCE_STATUS_PATH,
  LEAVE_BALANCES_PATH,
} from '@/features/attendance/config/attendancePaths';
import type { LeaveListFilters, LeaveSummary } from '@/features/attendance/types';

/**
 * 전 직원 휴가 현황 (관리자) 목록 page hook — 행 클릭 = 연결된 결재 문서 진입.
 * headerActions 는 페이지의 write 권한 게이트 안에서 렌더 — hook 은 권한을 재확인하지 않는다.
 */
export function useLeaveStatusListPage(): {
  api: ListApiConfig<LeaveSummary, LeaveListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<LeaveSummary, LeaveListFilters> = {
    menuCode: MENU_CODE.ATTENDANCE,
    useList: useGetLeavesQuery,
    rowKey: (m) => m.id,
    // 결재 문서 미연결 (approvalDocumentId null) 행은 무동작.
    onRowClick: (m) => {
      if (m.approvalDocumentId != null) {
        navigate(`${MENU_PATH[MENU_CODE.APPROVALS]}/${m.approvalDocumentId}`);
      }
    },
    emptyMessage: '조회된 휴가 신청이 없습니다.',
  };

  const headerActions: PageHeaderAction[] = [
    { design: 'secondary', label: '근태 현황', onClick: () => navigate(ATTENDANCE_STATUS_PATH) },
    { design: 'secondary', label: '잔여 관리', onClick: () => navigate(LEAVE_BALANCES_PATH) },
  ];

  return { api, headerActions };
}
