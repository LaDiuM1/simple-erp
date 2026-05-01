import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
} from '@/shared/ui/GenericList';
import CustomerStatusIndicator from '@/features/customer/components/CustomerStatusIndicator';
import {
  CUSTOMER_STATUS_OPTIONS,
  CUSTOMER_TYPE_LABELS,
  CUSTOMER_TYPE_OPTIONS,
  type CustomerSummary,
} from '@/features/customer/types';

export const customerListColumns: ColumnConfig<CustomerSummary>[] = [
  {
    key: 'code',
    label: '고객사 코드',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    width: 120,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.code}
      </Typography>
    ),
  },
  { key: 'name', label: '고객사명', sortable: true, sortDirection: 'asc', flex: 1.2 },
  { key: 'representative', label: '대표자', hideOnMobile: true, flex: 0.8 },
  { key: 'phone', label: '전화', hideOnMobile: true, flex: 1 },
  { key: 'roadAddress', label: '주소', sortable: true, sortDirection: 'asc', hideOnMobile: true, flex: 2.5 },
  {
    key: 'type',
    label: '분류',
    width: 120,
    render: (m) => CUSTOMER_TYPE_LABELS[m.type],
  },
  {
    key: 'tradeStartDate',
    label: '거래시작일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    hideOnMobile: true,
    width: 150,
  },
  {
    key: 'status',
    label: '상태',
    sortable: true,
    sortDirection: 'asc',
    width: 100,
    render: (m) => <CustomerStatusIndicator status={m.status} />,
  },
];

/**
 * 검색 모달용 컬럼 — 모바일에서는 고객사명 / 고객사 코드 / 전화번호만 노출.
 * 목록 페이지(`customerListColumns`)와 의도적으로 분리: 모달은 선택용이라 식별 정보만 필요.
 */
export const customerSelectColumns: ColumnConfig<CustomerSummary>[] = [
  {
    key: 'code',
    label: '고객사 코드',
    sortable: true,
    sortDirection: 'asc',
    width: 120,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.code}
      </Typography>
    ),
  },
  { key: 'name', label: '고객사명', sortable: true, sortDirection: 'asc', flex: 1.6 },
  { key: 'phone', label: '전화', flex: 1 },
  { key: 'bizRegNo', label: '사업자등록번호', hideOnMobile: true, width: 140 },
  { key: 'representative', label: '대표자', hideOnMobile: true, flex: 0.8 },
  { key: 'roadAddress', label: '주소', hideOnMobile: true, flex: 2 },
  {
    key: 'type',
    label: '분류',
    hideOnMobile: true,
    width: 80,
    render: (m) => CUSTOMER_TYPE_LABELS[m.type],
  },
  {
    key: 'status',
    label: '상태',
    sortable: true,
    sortDirection: 'asc',
    hideOnMobile: true,
    width: 96,
    render: (m) => <CustomerStatusIndicator status={m.status} />,
  },
];

export const customerListFilters: FilterConfig[] = [
  { type: 'search', key: 'codeKeyword', placeholder: '고객사 코드 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '고객사명 검색' },
  { type: 'search', key: 'addressKeyword', placeholder: '주소 검색' },
  { type: 'search', key: 'phoneKeyword', placeholder: '전화번호 검색' },
  { type: 'select', key: 'type', label: '분류', options: CUSTOMER_TYPE_OPTIONS, minWidth: 120 },
  { type: 'select', key: 'status', label: '상태', options: CUSTOMER_STATUS_OPTIONS, minWidth: 120 },
];
