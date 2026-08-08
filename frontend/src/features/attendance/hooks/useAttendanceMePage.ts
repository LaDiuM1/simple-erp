import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { todayIsoDate } from '@/shared/utils/date';
import {
  useCheckInMutation,
  useCheckOutMutation,
  useGetMyMonthlyAttendancesQuery,
} from '@/features/attendance/api/attendanceApi';
import {
  ATTENDANCE_STATUS_PATH,
  LEAVES_PATH,
} from '@/features/attendance/config/attendancePaths';
import { geolocationErrorMessage } from '@/features/attendance/utils/geolocation';
import { resolveAttendancePosition } from '@/features/attendance/utils/resolveAttendancePosition';
import { MONTH_FILTER_OPTIONS, yearFilterOptions } from '@/features/attendance/utils/periodOptions';
import { useDemo } from '@/shared/demo/DemoContext';

/**
 * 내 출퇴근 page hook — 오늘 카드 (GPS 출퇴근) + 월별 기록 조회 + headerActions 묶음.
 * 오늘 기록은 현재 년 / 월 고정 조회에서 도출 — 월 선택을 바꿔도 오늘 카드는 영향받지 않는다.
 */
export function useAttendanceMePage() {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.ATTENDANCE);
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const demo = useDemo();

  const currentYear = new Date().getFullYear();
  const currentMonth = new Date().getMonth() + 1;
  const [year, setYear] = useState(currentYear);
  const [month, setMonth] = useState(currentMonth);

  const monthlyQuery = useGetMyMonthlyAttendancesQuery({ year, month });
  const currentMonthQuery = useGetMyMonthlyAttendancesQuery({
    year: currentYear,
    month: currentMonth,
  });

  const [checkIn, { isLoading: isCheckingIn }] = useCheckInMutation();
  const [checkOut, { isLoading: isCheckingOut }] = useCheckOutMutation();
  const [locating, setLocating] = useState<'in' | 'out' | null>(null);

  const today = todayIsoDate();
  const todayAttendance = currentMonthQuery.data?.find((a) => a.workDate === today) ?? null;

  /** 좌표 확보 실패 (미지원 / 거부 / 타임아웃) 시 스낵바 에러 + null — 좌표 없이 요청을 보내지 않는다. */
  const resolvePosition = async (kind: 'in' | 'out') => {
    setLocating(kind);
    try {
      const position = await resolveAttendancePosition(demo.status);
      if (!position && demo.status.enabled) {
        snackbar.error('데모용 모의 위치가 준비되지 않아 출퇴근을 처리할 수 없습니다.');
      }
      return position;
    } catch (error) {
      snackbar.error(geolocationErrorMessage(error));
      return null;
    } finally {
      setLocating(null);
    }
  };

  const handleCheckIn = async () => {
    const position = await resolvePosition('in');
    if (!position) return;
    await submit(
      checkIn(position),
      { success: '출근 처리되었습니다.' },
    );
  };

  const handleCheckOut = async () => {
    const position = await resolvePosition('out');
    if (!position) return;
    await submit(
      checkOut(position),
      { success: '퇴근 처리되었습니다.' },
    );
  };

  const headerActions: PageHeaderAction[] = [
    { design: 'secondary', label: '휴가 관리', onClick: () => navigate(LEAVES_PATH) },
    ...(canWrite
      ? [
          {
            design: 'secondary' as const,
            label: '근태 현황',
            onClick: () => navigate(ATTENDANCE_STATUS_PATH),
          },
        ]
      : []),
  ];

  return {
    queries: { monthly: monthlyQuery },
    headerActions,
    today: {
      attendance: todayAttendance,
      // isFetching 포함 — 체크 성공 직후 refetch 완료 전 재클릭 (BE 409) 방지.
      isLoading: currentMonthQuery.isFetching
        || (demo.status.enabled && !demo.status.simulatedLocation),
      isCheckingIn: locating === 'in' || isCheckingIn,
      isCheckingOut: locating === 'out' || isCheckingOut,
      onCheckIn: handleCheckIn,
      onCheckOut: handleCheckOut,
      positionNotice: demo.status.enabled
        ? demo.status.simulatedLocation
          ? '데모에서는 실제 GPS 대신 공개된 합성 위치를 사용합니다.'
          : '데모용 모의 위치가 준비되지 않아 출퇴근 기능이 잠겨 있습니다.'
        : null,
    },
    monthFilter: {
      year,
      month,
      yearOptions: yearFilterOptions(),
      monthOptions: MONTH_FILTER_OPTIONS,
      onChangeYear: (v: number | null) => setYear(v ?? currentYear),
      onChangeMonth: (v: number | null) => setMonth(v ?? currentMonth),
    },
  };
}
