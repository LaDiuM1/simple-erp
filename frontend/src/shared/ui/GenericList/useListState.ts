import { useMemo, useState } from 'react';
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
}

/**
 * GenericList 내부 전용 상태 훅.
 * - searchFilter 로부터 emptyFilters 를 자동 생성 ("없음" = null 통일)
 * - column 에서 defaultSort: true 가 표시된 컬럼의 sortDirection 으로 initial sort 결정
 */
export function useListState<TFilters extends object>({
  searchFilter,
  column,
  pageSize = 10,
}: Config): ListState<TFilters> {
  const emptyFilters = useMemo(
    () => deriveEmptyFilters<TFilters>(searchFilter),
    [searchFilter],
  );
  const defaultSort = useMemo(() => deriveDefaultSort(column), [column]);

  const [filters, setFilters] = useState<TFilters>(emptyFilters);
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState<SortState>(defaultSort);

  const updateFilter = <K extends keyof TFilters>(key: K, value: TFilters[K]) => {
    setFilters((prev) => ({ ...prev, [key]: value }));
    setPage(0);
  };

  const resetFilters = () => {
    setFilters(emptyFilters);
    setPage(0);
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
    empty[item.key] = null;
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
