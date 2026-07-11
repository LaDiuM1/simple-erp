import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
  type SelectFilterItem,
} from '@/shared/ui/GenericList';
import { formatDateTime } from '@/shared/ui/GenericTabbedTable';
import ExpenseStatusIndicator from '@/features/expense/components/ExpenseStatusIndicator';
import { formatKrw } from '@/shared/utils/formatKrw';
import {
  EXPENSE_SCOPE_OPTIONS,
  EXPENSE_STATUS_OPTIONS,
  type ExpenseSummary,
} from '@/features/expense/types';

export const expenseListColumns: ColumnConfig<ExpenseSummary>[] = [
  {
    key: 'title',
    label: '제목',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    flex: 2.5,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.title}
      </Typography>
    ),
  },
  { key: 'claimantName', label: '청구자', flex: 1 },
  {
    key: 'itemCount',
    label: '항목 수',
    hideOnMobile: true,
    align: 'right',
    width: 90,
    render: (m) => `${m.itemCount}건`,
  },
  {
    key: 'totalAmount',
    label: '총액',
    sortable: true,
    sortDirection: 'desc',
    align: 'right',
    width: 140,
    render: (m) => formatKrw(m.totalAmount),
  },
  {
    key: 'status',
    label: '상태',
    width: 100,
    render: (m) => <ExpenseStatusIndicator status={m.status} />,
  },
  {
    key: 'createdAt',
    label: '청구일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    width: 150,
    render: (m) => formatDateTime(m.createdAt),
  },
];

export const expenseListFilters: FilterConfig[] = [
  { type: 'search', key: 'keyword', placeholder: '제목 검색' },
  { type: 'select', key: 'status', label: '상태', options: EXPENSE_STATUS_OPTIONS, minWidth: 120 },
  { type: 'date', key: 'startDate', label: '시작일' },
  { type: 'date', key: 'endDate', label: '종료일' },
];

/**
 * 조회 범위 필터 — EXPENSES 쓰기 권한자 (정산 담당) 에게만 page hook 이 동적으로 prepend.
 * null ('전체' 선택지) 은 api 레이어가 MINE 으로 매핑 — 기본과 동일하게 본인 청구만.
 */
export const expenseScopeFilter: SelectFilterItem = {
  type: 'select',
  key: 'scope',
  label: '조회 범위',
  options: EXPENSE_SCOPE_OPTIONS,
  defaultValue: 'MINE',
  minWidth: 130,
};
