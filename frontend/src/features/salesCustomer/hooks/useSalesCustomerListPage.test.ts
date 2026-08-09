import { renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import type { PageResponse } from '@/shared/types/api';
import type { SalesCustomerReference } from '@/features/customer/types';
import type { SalesCustomerAggregate } from '@/features/salesCustomer/types';
import { useSalesCustomerListQuery } from './useSalesCustomerListPage';

const mocks = vi.hoisted(() => ({
  customerQuery: vi.fn(),
  aggregateQuery: vi.fn(),
}));

vi.mock('@/features/customer/api/customerApi', () => ({
  useGetSalesCustomerReferencesQuery: (...args: unknown[]) => mocks.customerQuery(...args),
}));
vi.mock('@/features/salesCustomer/api/salesCustomerApi', () => ({
  useGetSalesCustomerAggregatesQuery: (...args: unknown[]) => mocks.aggregateQuery(...args),
}));

const customer = (id: number): SalesCustomerReference => ({
  id,
  code: `C${id}`,
  name: `고객사 ${id}`,
  representative: null,
  phone: null,
  type: 'GENERAL',
  status: 'ACTIVE',
  zipCode: null,
  roadAddress: null,
  detailAddress: null,
});

const customerPage = (ids: number[], page = 0): PageResponse<SalesCustomerReference> => ({
  content: ids.map(customer),
  page,
  size: 10,
  totalElements: ids.length,
  totalPages: ids.length === 0 ? 0 : 1,
  hasNext: false,
});

const aggregate = (customerId: number): SalesCustomerAggregate => ({
  customerId,
  primaryAssigneeId: customerId + 10,
  primaryAssigneeName: `담당자 ${customerId}`,
  activeAssigneeCount: 1,
  activityCount: customerId * 2,
  lastActivityDate: null,
});

const customerRefetch = vi.fn();
const aggregateRefetch = vi.fn();
let customerState: Record<string, unknown>;
let aggregateState: Record<string, unknown>;

const params = {
  codeKeyword: null,
  nameKeyword: null,
  addressKeyword: null,
  status: null,
  page: 0,
  size: 10,
  sort: 'name,asc',
};

function readyCustomerState(ids = [1, 2], page = 0) {
  const data = customerPage(ids, page);
  return {
    data,
    currentData: data,
    isLoading: false,
    isFetching: false,
    isError: false,
    error: undefined,
    refetch: customerRefetch,
  };
}

function readyAggregateState(ids = [1, 2]) {
  const data = ids.map(aggregate);
  return {
    data,
    currentData: data,
    isLoading: false,
    isFetching: false,
    isError: false,
    isUninitialized: false,
    error: undefined,
    refetch: aggregateRefetch,
  };
}

describe('useSalesCustomerListQuery', () => {
  beforeEach(() => {
    customerRefetch.mockReset();
    aggregateRefetch.mockReset();
    customerState = readyCustomerState();
    aggregateState = readyAggregateState();
    mocks.customerQuery.mockReset().mockImplementation(() => customerState);
    mocks.aggregateQuery.mockReset().mockImplementation(() => aggregateState);
  });

  it('고객 페이지와 정확히 일치하는 집계가 준비된 뒤에만 완성 행을 노출한다', () => {
    const { result } = renderHook(() => useSalesCustomerListQuery(params));

    expect(mocks.aggregateQuery).toHaveBeenLastCalledWith([1, 2], { skip: false });
    expect(result.current.currentData?.content).toEqual([
      expect.objectContaining({ id: 1, primaryAssigneeName: '담당자 1', activityCount: 2 }),
      expect.objectContaining({ id: 2, primaryAssigneeName: '담당자 2', activityCount: 4 }),
    ]);
    expect(result.current.isError).toBe(false);
  });

  it('집계가 아직 없으면 고객 기본 행이나 0 집계를 먼저 노출하지 않는다', () => {
    aggregateState = {
      data: undefined,
      currentData: undefined,
      isLoading: true,
      isFetching: true,
      isError: false,
      isUninitialized: false,
      refetch: aggregateRefetch,
    };

    const { result } = renderHook(() => useSalesCustomerListQuery(params));

    expect(result.current.currentData).toBeUndefined();
    expect(result.current.data).toBeUndefined();
    expect(result.current.isLoading).toBe(true);
  });

  it('조회 인자가 바뀌면 직전 완성본은 data에만 남기고 currentData에서는 숨긴다', () => {
    const { result, rerender } = renderHook(
      ({ page }) => useSalesCustomerListQuery({ ...params, page }),
      { initialProps: { page: 0 } },
    );
    expect(result.current.currentData?.content[0].id).toBe(1);

    customerState = {
      ...readyCustomerState(),
      currentData: undefined,
      isFetching: true,
    };
    rerender({ page: 1 });

    expect(result.current.data?.content[0].id).toBe(1);
    expect(result.current.currentData).toBeUndefined();
    expect(mocks.aggregateQuery).toHaveBeenLastCalledWith([], { skip: true });
  });

  it('새 고객 페이지가 집계보다 먼저 도착해도 직전 완성본 존재 신호를 유지한다', () => {
    const { result, rerender } = renderHook(() => useSalesCustomerListQuery(params));
    expect(result.current.currentData?.content.map((row) => row.id)).toEqual([1, 2]);

    customerState = readyCustomerState([3, 4]);
    aggregateState = {
      ...readyAggregateState([1, 2]),
      currentData: undefined,
      isFetching: true,
    };
    rerender();

    expect(result.current.data?.content.map((row) => row.id)).toEqual([1, 2]);
    expect(result.current.currentData).toBeUndefined();
    expect(result.current.isLoading).toBe(true);
    expect(result.current.isError).toBe(false);

    aggregateState = readyAggregateState([3, 4]);
    rerender();
    expect(result.current.currentData?.content.map((row) => row.id)).toEqual([3, 4]);
  });

  it('집계 갱신 오류는 마지막 정상 행을 유지하고 두 조회를 함께 재시도한다', () => {
    const { result, rerender } = renderHook(() => useSalesCustomerListQuery(params));
    aggregateState = {
      ...readyAggregateState(),
      isError: true,
      error: { status: 500, message: '집계 갱신 실패' },
    };
    rerender();

    expect(result.current.currentData?.content[0].primaryAssigneeName).toBe('담당자 1');
    expect(result.current.isError).toBe(true);
    expect(result.current.error).toEqual({ status: 500, message: '집계 갱신 실패' });

    result.current.refetch();
    expect(customerRefetch).toHaveBeenCalledOnce();
    expect(aggregateRefetch).toHaveBeenCalledOnce();
  });

  it('방어 경계에서도 잘못된 현재 집계 응답을 행으로 합성하지 않는다', () => {
    aggregateState = readyAggregateState([1]);
    const { result } = renderHook(() => useSalesCustomerListQuery(params));

    expect(result.current.currentData).toBeUndefined();
    expect(result.current.isError).toBe(true);
    expect(result.current.error).toEqual(expect.objectContaining({ status: 500 }));
  });

  it('빈 고객 페이지는 집계를 호출하지 않고 준비된 빈 결과로 처리한다', () => {
    customerState = readyCustomerState([]);
    aggregateState = {
      data: undefined,
      currentData: undefined,
      isLoading: false,
      isFetching: false,
      isError: false,
      isUninitialized: true,
      refetch: aggregateRefetch,
    };

    const { result } = renderHook(() => useSalesCustomerListQuery(params));

    expect(mocks.aggregateQuery).toHaveBeenLastCalledWith([], { skip: true });
    expect(result.current.currentData?.content).toEqual([]);
    expect(result.current.isLoading).toBe(false);
    result.current.refetch();
    expect(customerRefetch).toHaveBeenCalledOnce();
    expect(aggregateRefetch).not.toHaveBeenCalled();
  });

  it('현재 고객 조회 오류는 집계 상태보다 우선하고 이전 페이지를 숨긴다', () => {
    customerState = {
      ...readyCustomerState(),
      currentData: undefined,
      isError: true,
      error: { status: 503, message: '고객 조회 실패' },
    };

    const { result } = renderHook(() => useSalesCustomerListQuery(params));

    expect(result.current.currentData).toBeUndefined();
    expect(result.current.isError).toBe(true);
    expect(result.current.error).toEqual({ status: 503, message: '고객 조회 실패' });
    result.current.refetch();
    expect(customerRefetch).toHaveBeenCalledOnce();
    expect(aggregateRefetch).not.toHaveBeenCalled();
  });
});
