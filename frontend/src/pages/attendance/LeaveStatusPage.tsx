import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import PermissionGate from '@/shared/ui/layout/PermissionGate';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import { MENU_CODE } from '@/shared/config/menuConfig';
import {
  leaveStatusListColumns,
  leaveStatusListFilters,
} from '@/features/attendance/config/leaveStatusListConfig';
import { useLeaveStatusListPage } from '@/features/attendance/hooks/useLeaveStatusListPage';

/** 전 직원 휴가 현황 — 관리자 화면. BE 와 동일하게 write 권한 기준으로 접근 차단. */
export default function LeaveStatusPage() {
  const { api, headerActions } = useLeaveStatusListPage();

  return (
    <PermissionGate
      menuCode={MENU_CODE.ATTENDANCE}
      action="write"
      fallback={<ErrorScreen message="휴가 현황 조회 권한이 없습니다." fullScreen={false} />}
    >
      <PageHeaderActions actions={headerActions} />
      <GenericList
        api={api}
        searchFilter={leaveStatusListFilters}
        column={leaveStatusListColumns}
      />
    </PermissionGate>
  );
}
