import { useNavigate } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import {
  type ColumnConfig,
  type FilterConfig,
  type ListApiConfig,
} from '@/shared/ui/GenericList';
import {
  useDeleteCustomerMutation,
  useDownloadCustomersExcel,
  useGetCustomersQuery,
} from '@/features/customer/api/customerApi';
import CustomerStatusIndicator from '@/features/customer/components/CustomerStatusIndicator';
import {
  CUSTOMER_STATUS_OPTIONS,
  CUSTOMER_TYPE_LABELS,
  CUSTOMER_TYPE_OPTIONS,
  type CustomerListFilters,
  type CustomerSummary,
} from '@/features/customer/types';

export const customerColumn: ColumnConfig<CustomerSummary>[] = [
  {
    key: 'code',
    label: '고객사 코드',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.code}
      </Typography>
    ),
  },
  { key: 'name', label: '고객사명', sortable: true, sortDirection: 'asc' },
  { key: 'bizRegNo', label: '사업자등록번호', hideOnMobile: true },
  { key: 'representative', label: '대표자', hideOnMobile: true },
  { key: 'phone', label: '전화', hideOnMobile: true },
  { key: 'roadAddress', label: '주소', hideOnMobile: true },
  {
    key: 'type',
    label: '분류',
    render: (m) => CUSTOMER_TYPE_LABELS[m.type],
  },
  {
    key: 'status',
    label: '상태',
    sortable: true,
    sortDirection: 'asc',
    render: (m) => <CustomerStatusIndicator status={m.status} />,
  },
  {
    key: 'tradeStartDate',
    label: '거래시작일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    hideOnMobile: true,
  },
];

export const customerSearchFilter: FilterConfig[] = [
  { type: 'search', key: 'codeKeyword', placeholder: '고객사 코드 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '고객사명 검색' },
  { type: 'search', key: 'addressKeyword', placeholder: '주소 검색' },
  { type: 'search', key: 'phoneKeyword', placeholder: '전화번호 검색' },
  { type: 'select', key: 'type', label: '분류', options: CUSTOMER_TYPE_OPTIONS, minWidth: 120 },
  { type: 'select', key: 'status', label: '상태', options: CUSTOMER_STATUS_OPTIONS, minWidth: 120 },
];

/**
 * 행 클릭 = 상세 페이지 (읽기 권한). 편집 아이콘 = 수정 페이지 (쓰기 권한).
 * 권한 관리 목록과 동일한 진입 패턴 — GenericList 가 propagation 차단으로 두 액션이 경쟁하지 않게 처리.
 */
export function useCustomerListApi(): ListApiConfig<CustomerSummary, CustomerListFilters> {
  const navigate = useNavigate();
  return {
    menuCode: MENU_CODE.CUSTOMERS,
    useList: useGetCustomersQuery,
    useDelete: useDeleteCustomerMutation,
    useExcel: useDownloadCustomersExcel,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.CUSTOMERS]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.CUSTOMERS]}/${m.id}`),
  };
}
