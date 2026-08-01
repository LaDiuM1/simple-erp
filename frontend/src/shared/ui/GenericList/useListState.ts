import { useMemo } from 'react';
import {
  useResettableState,
  type StateResetKey,
} from '@/shared/hooks/useResettableState';
import type { FilterConfig, ListState, SortState } from './types';

/** useListState 가 defaultSort 도출에 쓰는 column 의 최소 shape (ColumnConfig 자동 호환) */
type ColumnSortView = {
  key: string;
  sortable?: boolean;
  sortDirection?: 'asc' | 'desc';
  defaultSort?: boolean;
};

interface Config {
  searchFilter: FilterConfig[];
  column: readonly ColumnSortView[];
  pageSize?: number;
  /** 검색 문맥이 바뀌면 필터/페이지/정렬을 함께 초기화한다. */
  resetKey?: StateResetKey;
}

/**
 * GenericList 내부 전용 상태 훅.
 * - searchFilter 로부터 emptyFilters 를 자동 생성 ("없음" = null 통일, defaultValue 지정 시 그 값)
 * - column 에서 defaultSort: true 가 표시된 컬럼의 sortDirection 으로 initial sort 결정
 */
export function useListState<TFilters extends object>({
  searchFilter,
  column,
  pageSize = 10,
  resetKey,
}: Config): ListState<TFilters> {
  const emptyFilters = useMemo(
    () => deriveEmptyFilters<TFilters>(searchFilter),
    [searchFilter],
  );
  const defaultSort = useMemo(() => deriveDefaultSort(column), [column]);

  const [state, setState] = useResettableState(resetKey, () => ({
    filters: emptyFilters,
    page: 0,
    sort: defaultSort,
  }));
  const { filters, page, sort } = state;

  const updateFilter = <K extends keyof TFilters>(key: K, value: TFilters[K]) => {
    setState((prev) => ({
      ...prev,
      filters: { ...prev.filters, [key]: value },
      page: 0,
    }));
  };

  const resetFilters = () => {
    setState((prev) => ({ ...prev, filters: emptyFilters, page: 0 }));
  };

  const setPage = (nextPage: number) => {
    setState((prev) => ({ ...prev, page: nextPage }));
  };

  const setSort = (nextSort: SortState) => {
    setState((prev) => ({ ...prev, sort: nextSort }));
  };

  const queryParams = useMemo(
    () => ({
      ...filters,
      page,
      size: pageSize,
      sort: `${sort.key},${sort.direction}`,
    }),
    [filters, page, pageSize, sort],
  );

  return {
    filters,
    updateFilter,
    resetFilters,
    page,
    setPage,
    pageSize,
    sort,
    setSort,
    queryParams,
  };
}

function deriveEmptyFilters<TFilters>(searchFilter: FilterConfig[]): TFilters {
  const empty: Record<string, unknown> = {};
  for (const item of searchFilter) {
    empty[item.key] = item.defaultValue ?? null;
  }
  return empty as TFilters;
}

function deriveDefaultSort(columns: readonly ColumnSortView[]): SortState {
  const marked = columns.find((c) => c.sortable && c.defaultSort);
  if (marked && marked.sortable && marked.sortDirection) {
    return { key: marked.key, direction: marked.sortDirection };
  }
  const firstSortable = columns.find((c) => c.sortable && c.sortDirection);
  if (firstSortable && firstSortable.sortDirection) {
    return { key: firstSortable.key, direction: firstSortable.sortDirection };
  }
  return { key: '', direction: 'asc' };
}
