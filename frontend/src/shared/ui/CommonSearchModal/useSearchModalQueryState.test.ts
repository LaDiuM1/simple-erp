import { act, renderHook } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import {
  mergeFixedQueryParams,
  useSearchModalQueryState,
} from './useSearchModalQueryState';

describe('mergeFixedQueryParams', () => {
  it('문맥 고정 조건이 화면 필터의 같은 키보다 우선한다', () => {
    expect(
      mergeFixedQueryParams(
        { page: 0, size: 10, status: 'RESIGNED' },
        { status: 'ACTIVE' },
      ),
    ).toEqual({ page: 0, size: 10, status: 'ACTIVE' });
  });
});

describe('useSearchModalQueryState', () => {
  it('검색 scope가 바뀌면 페이지와 화면 필터를 초기화한다', () => {
    interface Filters {
      keyword?: string | null;
      customerId?: number | null;
    }

    const useList = vi.fn(() => ({
      data: undefined,
      isFetching: false,
      isError: false,
      refetch: vi.fn(),
    }));
    const api = {
      useList,
      rowKey: (row: { id: number }) => row.id,
    };
    const searchFilter = [{ type: 'search' as const, key: 'keyword' }];
    const column = [{ key: 'id', label: 'ID' }];

    const { result, rerender } = renderHook(
      ({ scopeKey, customerId }) => useSearchModalQueryState<{ id: number }, Filters>({
        api,
        searchFilter,
        column,
        fixedQueryParams: { customerId },
        scopeKey,
      }),
      { initialProps: { scopeKey: 'customer:1', customerId: 1 } },
    );

    act(() => {
      result.current.state.updateFilter('keyword', '작성 중');
      result.current.state.setPage(3);
    });
    expect(result.current.state.filters).toEqual({ keyword: '작성 중' });
    expect(result.current.state.page).toBe(3);

    rerender({ scopeKey: 'customer:2', customerId: 2 });

    expect(result.current.state.filters).toEqual({ keyword: null });
    expect(result.current.state.page).toBe(0);
    expect(useList).toHaveBeenLastCalledWith(expect.objectContaining({
      customerId: 2,
      keyword: null,
      page: 0,
    }));
  });
});
