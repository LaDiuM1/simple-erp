import { useNavigate } from 'react-router-dom';
import { MENU_CODE } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useGetAttendancesQuery } from '@/features/attendance/api/attendanceApi';
import { LEAVE_STATUS_PATH } from '@/features/attendance/config/attendancePaths';
import type { Attendance, AttendanceListFilters } from '@/features/attendance/types';

/**
 * 전 직원 근태 현황 (관리자) 목록 page hook — 조회 전용 (등록 / 수정 / 삭제 없음).
 * headerActions 는 페이지의 write 권한 게이트 안에서 렌더 — hook 은 권한을 재확인하지 않는다.
 */
export function useAttendanceStatusListPage(): {
  api: ListApiConfig<Attendance, AttendanceListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<Attendance, AttendanceListFilters> = {
    menuCode: MENU_CODE.ATTENDANCE,
    useList: useGetAttendancesQuery,
    rowKey: (a) => a.id,
    emptyMessage: '조회된 근태 기록이 없습니다.',
  };

  const headerActions: PageHeaderAction[] = [
    { design: 'secondary', label: '휴가 현황', onClick: () => navigate(LEAVE_STATUS_PATH) },
  ];

  return { api, headerActions };
}
