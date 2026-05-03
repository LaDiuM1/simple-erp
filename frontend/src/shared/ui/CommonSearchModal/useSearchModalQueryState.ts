import { useMemo } from 'react';
import { useListState, type ColumnConfig, type FilterConfig } from '@/shared/ui/GenericList';
import type { CommonSearchModalApi } from './types';

interface Args<TRow, TFilters extends object> {
  api: CommonSearchModalApi<TRow, TFilters>;
  searchFilter?: FilterConfig[];
  column: ColumnConfig<TRow>[];
  /** select 모드 전용 — 결과에서 제외할 id 목록. manage 모드에서는 미지정. */
  excludeIds?: number[];
}

/**
 * 모달의 페이징 / 필터 / 정렬 / RTK Query 호출을 묶은 공유 hook.
 * select / manage 모드 모두 같은 검색 흐름을 사용하므로 두 컴포넌트가 이 hook 으로 상태를 일원화한다.
 */
export function useSearchModalQueryState<TRow, TFilters extends object>({
  api,
  searchFilter,
  column,
  excludeIds,
}: Args<TRow, TFilters>) {
  const state = useListState<TFilters>({
    searchFilter: searchFilter ?? [],
    column,
    pageSize: api.pageSize ?? 10,
  });
  const query = api.useList(state.queryParams);

  const visibleRows = useMemo(() => {
    const rows = query.data?.content ?? [];
    if (!excludeIds || excludeIds.length === 0) return rows;
    const set = new Set(excludeIds);
    return rows.filter((row) => !set.has(api.rowKey(row)));
  }, [query.data, api, excludeIds]);

  return { state, query, visibleRows };
}
