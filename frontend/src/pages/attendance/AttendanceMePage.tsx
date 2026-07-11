import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import FilterSelect from '@/shared/ui/atoms/FilterSelect';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import TodayAttendanceCard from '@/features/attendance/components/TodayAttendanceCard/TodayAttendanceCard';
import MonthlyAttendanceTable from '@/features/attendance/components/MonthlyAttendanceTable/MonthlyAttendanceTable';
import {
  SurfaceHeaderRow,
  SurfaceRoot,
  SurfaceTitle,
} from '@/features/attendance/components/attendanceSurface.styles';
import { useAttendanceMePage } from '@/features/attendance/hooks/useAttendanceMePage';
import { MonthlyFilterGroup, PageRoot } from './AttendanceMePage.styles';

/** 내 출퇴근 — 오늘 카드 (GPS 출퇴근) + 월별 기록. 커스텀 페이지 (page hook + sub-component). */
export default function AttendanceMePage() {
  const { queries, headerActions, today, monthFilter } = useAttendanceMePage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <PageRoot>
        <TodayAttendanceCard {...today} />

        <SurfaceRoot>
          <SurfaceHeaderRow>
            <SurfaceTitle>월별 출퇴근 기록</SurfaceTitle>
            <MonthlyFilterGroup>
              <FilterSelect
                label="년도"
                allLabel="올해"
                value={monthFilter.year}
                onChange={monthFilter.onChangeYear}
                options={monthFilter.yearOptions}
                minWidth={110}
              />
              <FilterSelect
                label="월"
                allLabel="이번 달"
                value={monthFilter.month}
                onChange={monthFilter.onChangeMonth}
                options={monthFilter.monthOptions}
                minWidth={96}
              />
            </MonthlyFilterGroup>
          </SurfaceHeaderRow>
          <QueryGate queries={queries}>
            {({ monthly }) => <MonthlyAttendanceTable rows={monthly} />}
          </QueryGate>
        </SurfaceRoot>
      </PageRoot>
    </>
  );
}
