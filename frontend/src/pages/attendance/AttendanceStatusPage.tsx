import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import PermissionGate from '@/shared/ui/layout/PermissionGate';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import { MENU_CODE } from '@/shared/config/menuConfig';
import {
  attendanceStatusListColumns,
  attendanceStatusListFilters,
} from '@/features/attendance/config/attendanceStatusListConfig';
import { useAttendanceStatusListPage } from '@/features/attendance/hooks/useAttendanceStatusListPage';

/** 전 직원 근태 현황 — 관리자 화면. BE 와 동일하게 write 권한 기준으로 접근 차단. */
export default function AttendanceStatusPage() {
  const { api, headerActions } = useAttendanceStatusListPage();

  return (
    <PermissionGate
      menuCode={MENU_CODE.ATTENDANCE}
      action="write"
      fallback={<ErrorScreen message="근태 현황 조회 권한이 없습니다." fullScreen={false} />}
    >
      <PageHeaderActions actions={headerActions} />
      <GenericList
        api={api}
        searchFilter={attendanceStatusListFilters}
        column={attendanceStatusListColumns}
      />
    </PermissionGate>
  );
}
