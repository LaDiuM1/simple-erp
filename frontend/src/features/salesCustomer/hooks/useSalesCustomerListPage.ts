import { useCallback, useEffect, useMemo, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type {
  ListApiConfig,
  ListQueryParamsBase,
  QueryState,
} from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import type { ApiError, PageResponse } from '@/shared/types/api';
import { useGetSalesCustomerReferencesQuery } from '@/features/customer/api/customerApi';
import {
  type CustomerListFilters,
  type SalesCustomerReference,
} from '@/features/customer/types';
import { useGetSalesCustomerAggregatesQuery } from '@/features/salesCustomer/api/salesCustomerApi';
import {
  getSalesCustomerAggregateContractError,
  SALES_CUSTOMER_AGGREGATE_CONTRACT_MESSAGE,
} from '@/features/salesCustomer/utils/salesCustomerAggregateContract';
import type { SalesCustomerAggregate } from '@/features/salesCustomer/types';
import type { SalesCustomerListRow } from '@/features/salesCustomer/config/salesCustomerListConfig';

const AGGREGATE_CONTRACT_ERROR: ApiError = {
  status: 500,
  message: SALES_CUSTOMER_AGGREGATE_CONTRACT_MESSAGE,
};

type CompositionResult =
  | { data: PageResponse<SalesCustomerListRow>; error?: never }
  | { data?: never; error: ApiError };

/** 렌더가 확정된 마지막 완성본만 다음 조회 전환의 `data` 신호로 유지한다. */
function useLastCommittedPage<T>(page: PageResponse<T> | undefined) {
  const committedPage = useRef<PageResponse<T> | undefined>(undefined);
  useEffect(() => {
    if (page) committedPage.current = page;
  }, [page]);
  // React Compiler 규칙상 렌더 중 ref 읽기를 경고하지만, effect 에서 확정된 읽기 전용 스냅샷이다.
  // eslint-disable-next-line react-hooks/refs
  return page ?? committedPage.current;
}

/** 고객 페이지와 집계 응답의 ID 가 정확히 일치할 때만 완성된 행을 만든다. */
export function composeSalesCustomerPage(
  customers: PageResponse<SalesCustomerReference>,
  aggregates: SalesCustomerAggregate[],
): CompositionResult {
  const customerIds = customers.content.map((customer) => customer.id);
  if (getSalesCustomerAggregateContractError(aggregates, customerIds)) {
    return { error: AGGREGATE_CONTRACT_ERROR };
  }

  const aggregateById = new Map(aggregates.map((aggregate) => [aggregate.customerId, aggregate]));
  return {
    data: {
      ...customers,
      content: customers.content.map<SalesCustomerListRow>((customer) => {
        const aggregate = aggregateById.get(customer.id)!;
        return {
          ...customer,
          primaryAssigneeName: aggregate.primaryAssigneeName,
          activeAssigneeCount: aggregate.activeAssigneeCount,
          activityCount: aggregate.activityCount,
          lastActivityDate: aggregate.lastActivityDate,
        };
      }),
    },
  };
}

/**
 * customer 페이지 + sales aggregates 합성 — GenericList 의 useList 인터페이스에 맞춰 단일 훅으로 감싼다.
 * 활성 페이지의 customerIds 만 집계 호출.
 */
export function useSalesCustomerListQuery(
  params: CustomerListFilters & ListQueryParamsBase,
): QueryState<SalesCustomerListRow> {
  const customers = useGetSalesCustomerReferencesQuery(params);
  const customerPage = customers.currentData;
  const customerIds = useMemo(
    () => customerPage?.content.map((customer) => customer.id) ?? [],
    [customerPage],
  );
  const aggregatesRequired = customerIds.length > 0;
  const aggregates = useGetSalesCustomerAggregatesQuery(customerIds, {
    skip: !aggregatesRequired,
  });
  const composition = useMemo<CompositionResult | undefined>(() => {
    if (!customerPage) return undefined;
    if (!aggregatesRequired) return composeSalesCustomerPage(customerPage, []);
    if (!aggregates.currentData) return undefined;
    return composeSalesCustomerPage(customerPage, aggregates.currentData);
  }, [aggregates.currentData, aggregatesRequired, customerPage]);

  const contractError = composition?.error;
  const isAggregateError = aggregatesRequired && aggregates.isError;
  const error = customers.isError
    ? customers.error
    : isAggregateError
      ? aggregates.error
      : contractError;
  const isError = customers.isError || isAggregateError || contractError !== undefined;
  const merged = composition?.data;
  const currentData = merged;
  const latestData = useLastCommittedPage(currentData);
  const refetchCustomers = customers.refetch;
  const refetchAggregates = aggregates.refetch;
  const aggregatesUninitialized = aggregates.isUninitialized;
  const refetch = useCallback(() => {
    refetchCustomers();
    if (aggregatesRequired && !aggregatesUninitialized) {
      refetchAggregates();
    }
  }, [aggregatesRequired, aggregatesUninitialized, refetchAggregates, refetchCustomers]);

  return {
    data: latestData,
    currentData,
    isLoading: currentData === undefined && !isError,
    isFetching: customers.isFetching || aggregates.isFetching,
    isError,
    error,
    refetch,
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
