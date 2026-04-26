import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import {
  type ColumnConfig,
  type FilterConfig,
  type ListApiConfig,
  type ListQueryParamsBase,
  type QueryState,
} from '@/shared/ui/GenericList';
import { useGetCustomersQuery } from '@/features/customer/api/customerApi';
import {
  CUSTOMER_STATUS_OPTIONS,
  type CustomerListFilters,
  type CustomerSummary,
} from '@/features/customer/types';
import CustomerStatusIndicator from '@/features/customer/components/CustomerStatusIndicator';
import {
  useGetSalesCustomerAggregatesQuery,
} from '@/features/salesCustomer/api/salesCustomerApi';

/**
 * 영업 관리 목록 행 — customer 마스터 + 영업 집계 (현담당자 / 인원 / 활동수 / 마지막 활동일).
 */
export type SalesCustomerListRow = CustomerSummary & {
  primaryAssigneeName: string | null;
  activeAssigneeCount: number;
  activityCount: number;
  lastActivityDate: string | null;
};

export const salesCustomerListColumn: ColumnConfig<SalesCustomerListRow>[] = [
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
  {
    key: 'status',
    label: '거래 상태',
    render: (m) => <CustomerStatusIndicator status={m.status} />,
  },
  { key: 'primaryAssigneeName', label: '주담당자' },
  {
    key: 'activeAssigneeCount',
    label: '담당 인원',
    align: 'right',
    render: (m) => `${m.activeAssigneeCount}명`,
  },
  {
    key: 'activityCount',
    label: '활동 수',
    align: 'right',
    hideOnMobile: true,
    render: (m) => `${m.activityCount}건`,
  },
  {
    key: 'lastActivityDate',
    label: '마지막 활동일',
    hideOnMobile: true,
    render: (m) => (m.lastActivityDate ? m.lastActivityDate.replace('T', ' ').slice(0, 16) : null),
  },
];

export const salesCustomerListSearchFilter: FilterConfig[] = [
  { type: 'search', key: 'codeKeyword', placeholder: '고객사 코드 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '고객사명 검색' },
  { type: 'search', key: 'addressKeyword', placeholder: '주소 검색' },
  { type: 'select', key: 'status', label: '상태', options: CUSTOMER_STATUS_OPTIONS, minWidth: 120 },
];

/**
 * customer 페이지 + sales aggregates 합성 — GenericList 의 useList 인터페이스에 맞춰 단일 훅으로 감싼다.
 * 활성 페이지의 customerIds 만 집계 호출.
 */
function useSalesCustomerListQuery(
  params: CustomerListFilters & ListQueryParamsBase,
): QueryState<SalesCustomerListRow> {
  const customers = useGetCustomersQuery(params);
  const customerIds = customers.data?.content.map((c) => c.id) ?? [];
  const aggregates = useGetSalesCustomerAggregatesQuery(customerIds, {
    skip: customerIds.length === 0,
  });

  const merged = useMemo(() => {
    if (!customers.data) return undefined;
    const aggMap = new Map((aggregates.data ?? []).map((a) => [a.customerId, a]));
    return {
      ...customers.data,
      content: customers.data.content.map<SalesCustomerListRow>((c) => {
        const agg = aggMap.get(c.id);
        return {
          ...c,
          primaryAssigneeName: agg?.primaryAssigneeName ?? null,
          activeAssigneeCount: agg?.activeAssigneeCount ?? 0,
          activityCount: agg?.activityCount ?? 0,
          lastActivityDate: agg?.lastActivityDate ?? null,
        };
      }),
    };
  }, [customers.data, aggregates.data]);

  return {
    data: merged,
    isFetching: customers.isFetching || aggregates.isFetching,
    isError: customers.isError,
    error: customers.error,
    refetch: customers.refetch,
  };
}

export function useSalesCustomerListApi(): ListApiConfig<SalesCustomerListRow, CustomerListFilters> {
  const navigate = useNavigate();
  return {
    menuCode: MENU_CODE.SALES_CUSTOMERS,
    useList: useSalesCustomerListQuery,
    rowKey: (m) => m.id,
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.SALES_CUSTOMERS]}/${m.id}`),
  };
}
