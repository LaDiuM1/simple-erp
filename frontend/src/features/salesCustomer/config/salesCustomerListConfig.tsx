import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
} from '@/shared/ui/GenericList';
import {
  CUSTOMER_STATUS_OPTIONS,
  type CustomerSummary,
} from '@/features/customer/types';
import CustomerStatusIndicator from '@/features/customer/components/CustomerStatusIndicator';

/**
 * 영업 관리 목록 행 — customer 마스터 + 영업 집계 (현담당자 / 인원 / 활동수 / 마지막 활동일).
 */
export type SalesCustomerListRow = CustomerSummary & {
  primaryAssigneeName: string | null;
  activeAssigneeCount: number;
  activityCount: number;
  lastActivityDate: string | null;
};

export const salesCustomerListColumns: ColumnConfig<SalesCustomerListRow>[] = [
  {
    key: 'code',
    label: '고객사 코드',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    flex: 1,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.code}
      </Typography>
    ),
  },
  { key: 'name', label: '고객사명', sortable: true, sortDirection: 'asc', flex: 1 },
  { key: 'primaryAssigneeName', label: '주담당자', flex: 1 },
  {
    key: 'activeAssigneeCount',
    label: '담당 인원',
    align: 'right',
    flex: 1,
    render: (m) => `${m.activeAssigneeCount}명`,
  },
  {
    key: 'activityCount',
    label: '활동 수',
    align: 'right',
    hideOnMobile: true,
    flex: 1,
    render: (m) => `${m.activityCount}건`,
  },
  {
    key: 'lastActivityDate',
    label: '마지막 활동일',
    hideOnMobile: true,
    flex: 1,
    render: (m) => (m.lastActivityDate ? m.lastActivityDate.replace('T', ' ').slice(0, 16) : null),
  },
  {
    key: 'status',
    label: '거래 상태',
    width: 110,
    render: (m) => <CustomerStatusIndicator status={m.status} />,
  },
];

export const salesCustomerListFilters: FilterConfig[] = [
  { type: 'search', key: 'codeKeyword', placeholder: '고객사 코드 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '고객사명 검색' },
  { type: 'search', key: 'addressKeyword', placeholder: '주소 검색' },
  { type: 'select', key: 'status', label: '상태', options: CUSTOMER_STATUS_OPTIONS, minWidth: 120 },
];
