import Typography from '@mui/material/Typography';
import Muted from '@/shared/ui/atoms/Muted';
import {
  type ColumnConfig,
  type FilterConfig,
} from '@/shared/ui/GenericList';
import { formatDateTime } from '@/shared/ui/GenericTabbedTable';
import EmployeeSelectFilter from '@/features/employee/components/EmployeeSelectFilter';
import LeaveStatusIndicator from '@/features/attendance/components/LeaveStatusIndicator';
import { formatLeavePeriod } from '@/features/attendance/utils/format';
import {
  LEAVE_STATUS_OPTIONS,
  LEAVE_TYPE_LABELS,
  type LeaveSummary,
} from '@/features/attendance/types';

export const leaveStatusListColumns: ColumnConfig<LeaveSummary>[] = [
  {
    key: 'employeeName',
    label: '직원',
    mobilePrimary: true,
    flex: 1,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.employeeName}
      </Typography>
    ),
  },
  {
    key: 'leaveType',
    label: '유형',
    width: 100,
    render: (m) => LEAVE_TYPE_LABELS[m.leaveType],
  },
  {
    key: 'startDate',
    label: '기간',
    sortable: true,
    sortDirection: 'desc',
    width: 200,
    render: (m) => formatLeavePeriod(m.startDate, m.endDate),
  },
  {
    key: 'days',
    label: '일수',
    width: 72,
    render: (m) => `${m.days}일`,
  },
  {
    key: 'status',
    label: '상태',
    width: 90,
    render: (m) => <LeaveStatusIndicator status={m.status} />,
  },
  {
    key: 'reason',
    label: '사유',
    hideOnMobile: true,
    flex: 2,
    render: (m) => m.reason ?? <Muted />,
  },
  {
    key: 'createdAt',
    label: '신청일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    hideOnMobile: true,
    width: 150,
    render: (m) => formatDateTime(m.createdAt),
  },
];

/** startDate / endDate 는 휴가 기간과의 겹침 검색 (BE 계약). */
export const leaveStatusListFilters: FilterConfig[] = [
  { type: 'select', key: 'status', label: '상태', options: LEAVE_STATUS_OPTIONS, minWidth: 110 },
  { type: 'date', key: 'startDate', label: '시작일' },
  { type: 'date', key: 'endDate', label: '종료일' },
  {
    type: 'custom',
    key: 'employeeId',
    render: ({ value, onChange }) => (
      <EmployeeSelectFilter value={value} onChange={onChange} />
    ),
  },
];
