import Muted from '@/shared/ui/atoms/Muted';
import WithinRangeText from '@/features/attendance/components/WithinRangeText';
import { todayIsoDate } from '@/shared/utils/date';
import { formatDateWithDay, formatTime } from '@/features/attendance/utils/format';
import type { Attendance } from '@/features/attendance/types';
import {
  ButtonRow,
  CardDate,
  CardHeaderRow,
  CardRoot,
  CardTitle,
  CheckInButton,
  CheckOutButton,
  StatBlock,
  StatGrid,
  StatLabel,
  StatSub,
  StatValue,
  PositionNotice,
} from './TodayAttendanceCard.styles';

export interface TodayAttendanceCardProps {
  /** 오늘 출퇴근 기록 — 없으면 미출근 (BE 는 오늘 행 존재 = 이미 출근). */
  attendance: Attendance | null;
  /** 오늘 기록 조회 / 재조회 중 — 상태 확정 전에는 버튼 비활성 (체크 직후 재클릭 409 방지). */
  isLoading: boolean;
  isCheckingIn: boolean;
  isCheckingOut: boolean;
  onCheckIn: () => void;
  onCheckOut: () => void;
  /** 데모 환경에서는 브라우저 GPS를 요청하지 않고 합성 좌표를 사용한다. */
  positionNotice?: string | null;
}

/**
 * 오늘 출퇴근 카드 — 출근 / 퇴근 버튼 + 체크 시각 + 사무실 반경 내 여부 (텍스트 색으로만 구분).
 * 위치 확보 실패 시의 스낵바 에러는 page hook 이 처리 — 카드는 표현만 담당.
 */
export default function TodayAttendanceCard({
  attendance,
  isLoading,
  isCheckingIn,
  isCheckingOut,
  onCheckIn,
  onCheckOut,
  positionNotice,
}: TodayAttendanceCardProps) {
  const checkInTime = formatTime(attendance?.checkInAt ?? null);
  const checkOutTime = formatTime(attendance?.checkOutAt ?? null);
  const canCheckIn = !isLoading && attendance === null;
  const canCheckOut = !isLoading && checkInTime !== null && checkOutTime === null;

  return (
    <CardRoot>
      <CardHeaderRow>
        <CardTitle>오늘 출퇴근</CardTitle>
        <CardDate>{formatDateWithDay(todayIsoDate())}</CardDate>
      </CardHeaderRow>

      <StatGrid>
        <StatBlock>
          <StatLabel>출근</StatLabel>
          <StatValue>{checkInTime ?? <Muted />}</StatValue>
          {attendance?.checkInAt && (
            <StatSub>
              <WithinRangeText withinRange={attendance.checkInWithinRange} />
            </StatSub>
          )}
        </StatBlock>
        <StatBlock>
          <StatLabel>퇴근</StatLabel>
          <StatValue>{checkOutTime ?? <Muted />}</StatValue>
          {attendance?.checkOutAt && (
            <StatSub>
              <WithinRangeText withinRange={attendance.checkOutWithinRange} />
            </StatSub>
          )}
        </StatBlock>
      </StatGrid>

      <ButtonRow>
        <CheckInButton onClick={onCheckIn} disabled={!canCheckIn || isCheckingIn}>
          {isCheckingIn ? '처리 중...' : '출근'}
        </CheckInButton>
        <CheckOutButton onClick={onCheckOut} disabled={!canCheckOut || isCheckingOut}>
          {isCheckingOut ? '처리 중...' : '퇴근'}
        </CheckOutButton>
      </ButtonRow>
      {positionNotice && <PositionNotice>{positionNotice}</PositionNotice>}
    </CardRoot>
  );
}
