import Typography from '@mui/material/Typography';
import Muted from '@/shared/ui/atoms/Muted';
import {
  type ColumnConfig,
  type FilterConfig,
} from '@/shared/ui/GenericList';
import WithinRangeText from '@/features/attendance/components/WithinRangeText';
import AttendanceEmployeeFilter from '@/features/attendance/components/AttendanceEmployeeFilter';
import { formatDateWithDay, formatTime } from '@/features/attendance/utils/format';
import { MONTH_FILTER_OPTIONS, yearFilterOptions } from '@/features/attendance/utils/periodOptions';
import type { Attendance } from '@/features/attendance/types';

export const attendanceStatusListColumns: ColumnConfig<Attendance>[] = [
  {
    key: 'employeeName',
    label: '직원',
    mobilePrimary: true,
    flex: 1,
    render: (a) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {a.employeeName}
      </Typography>
    ),
  },
  {
    key: 'workDate',
    label: '일자',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    width: 160,
    render: (a) => formatDateWithDay(a.workDate),
  },
  {
    key: 'checkInAt',
    label: '출근 시각',
    flex: 1,
    render: (a) => formatTime(a.checkInAt) ?? <Muted />,
  },
  {
    key: 'checkOutAt',
    label: '퇴근 시각',
    flex: 1,
    render: (a) => formatTime(a.checkOutAt) ?? <Muted />,
  },
  {
    key: 'checkInWithinRange',
    label: '출근 위치',
    hideOnMobile: true,
    flex: 1,
    render: (a) =>
      a.checkInAt ? <WithinRangeText withinRange={a.checkInWithinRange} /> : <Muted />,
  },
  {
    key: 'checkOutWithinRange',
    label: '퇴근 위치',
    hideOnMobile: true,
    flex: 1,
    render: (a) =>
      a.checkOutAt ? <WithinRangeText withinRange={a.checkOutWithinRange} /> : <Muted />,
  },
];

const CURRENT_YEAR = new Date().getFullYear();
const CURRENT_MONTH = new Date().getMonth() + 1;

/**
 * BE 는 year / month 필수 — defaultValue 로 현재 년 / 월을 초기 선택.
 * null 선택 시에는 api 쪽 fallback 이 현재 년 / 월로 매핑하므로
 * allLabel 을 '전체' 대신 '올해' / '이번 달' 로 표기해 오해를 막는다.
 */
export const attendanceStatusListFilters: FilterConfig[] = [
  {
    type: 'select',
    key: 'year',
    label: '년도',
    options: yearFilterOptions(),
    defaultValue: CURRENT_YEAR,
    allLabel: '올해',
    minWidth: 120,
  },
  {
    type: 'select',
    key: 'month',
    label: '월',
    options: MONTH_FILTER_OPTIONS,
    defaultValue: CURRENT_MONTH,
    allLabel: '이번 달',
    minWidth: 100,
  },
  {
    type: 'custom',
    key: 'employeeId',
    render: ({ value, onChange }) => (
      <AttendanceEmployeeFilter value={value} onChange={onChange} />
    ),
  },
];
