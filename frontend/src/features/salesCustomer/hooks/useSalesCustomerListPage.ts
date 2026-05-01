import { useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type {
  ListApiConfig,
  ListQueryParamsBase,
  QueryState,
} from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useGetCustomersQuery } from '@/features/customer/api/customerApi';
import {
  type CustomerListFilters,
} from '@/features/customer/types';
import { useGetSalesCustomerAggregatesQuery } from '@/features/salesCustomer/api/salesCustomerApi';
import type { SalesCustomerListRow } from '@/features/salesCustomer/config/salesCustomerListConfig';

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

/**
 * 영업 관리 목록 page hook — api + headerActions 묶음. 헤더 액션 없음 (등록은 고객사 모듈 담당).
 */
export function useSalesCustomerListPage(): {
  api: ListApiConfig<SalesCustomerListRow, CustomerListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<SalesCustomerListRow, CustomerListFilters> = {
    menuCode: MENU_CODE.SALES_CUSTOMERS,
    useList: useSalesCustomerListQuery,
    rowKey: (m) => m.id,
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.SALES_CUSTOMERS]}/${m.id}`),
  };

  return { api, headerActions: [] };
}
