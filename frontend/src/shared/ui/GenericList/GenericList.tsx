import DownloadIcon from '@mui/icons-material/FileDownloadOutlined';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import type { ApiError } from '@/shared/types/api';
import ListSearchFilter from './ListSearchFilter';
import ListTable from './ListTable';
import ListPagination from './ListPagination';
import { useListState } from './useListState';
import {
  ExcelDownloadButton,
  FilterBarArea,
  ListRoot,
  ListSurface,
} from './GenericList.styles';
import type {
  ColumnConfig,
  FilterConfig,
  ListApiConfig,
  SortState,
  UseDeleteMutation,
  UseExcelDownload,
} from './types';
import type { ListSelectionState } from './useListSelection';

const DEFAULT_DELETE_SUCCESS = '삭제되었습니다.';
const DEFAULT_DELETE_ERROR = '삭제 중 오류가 발생했습니다.';

/**
 * api.useDelete 가 미지정인 페이지에서 hooks 호출 순서 유지용 placeholder.
 * 실제 deleteFn 은 호출되지 않는다 (handleDelete 가 undefined 로 전달되어 ListTable 이 행 삭제 아이콘 자체를 그리지 않음).
 */
const noopDeleteHook: UseDeleteMutation = () =>
  [() => ({ unwrap: async () => undefined }), undefined] as const;

export interface GenericListProps<TRow, TFilters extends object> {
  api: ListApiConfig<TRow, TFilters>;
  searchFilter: FilterConfig[];
  column: ColumnConfig<TRow>[];
  /**
   * 행 선택 상태. api.checkBox 가 true 일 때 함께 전달해야 체크박스가 렌더된다.
   * 페이지가 useListSelection() 으로 생성하여 일괄 액션 버튼과 공유.
   */
  selection?: ListSelectionState;
}

/**
 * 도메인 목록 본문 — 검색바 / 테이블 / 페이지네이션 / 행 삭제 확인 / 엑셀 다운로드까지 합성.
 * 페이지 헤더는 GenericList 밖에서 페이지가 직접 조립한다.
 */
export default function GenericList<TRow, TFilters extends object>({
  api,
  searchFilter,
  column,
  selection,
}: GenericListProps<TRow, TFilters>) {
  const state = useListState<TFilters>({
    searchFilter,
    column,
    pageSize: api.pageSize,
  });
  const snackbar = useSnackbar();

  const query = api.useList(state.queryParams);
  const { data, isFetching, isError, error, refetch } = query;

  // useDelete 가 없는 페이지는 noopDeleteHook 으로 hooks 순서 유지 + onDelete 미전달로 행 삭제 아이콘 비표시.
  const useDeleteHook = api.useDelete ?? noopDeleteHook;
  const [deleteFn] = useDeleteHook();

  const handleDelete = api.useDelete
    ? async (row: TRow) => {
        try {
          const id = api.rowKey(row);
          await deleteFn(id).unwrap();
          snackbar.success(api.successMessages?.delete ?? DEFAULT_DELETE_SUCCESS);
          if (selection?.isSelected(id)) {
            selection.toggle(id);
          }
        } catch (err) {
          snackbar.error((err as ApiError)?.message ?? DEFAULT_DELETE_ERROR);
          throw err;
        }
      }
    : undefined;

  if (isError) {
    return <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />;
  }

  // 체크박스 활성화 여부 — api.checkBox 가 true 이고 selection prop 이 전달된 경우.
  const checkboxEnabled = api.checkBox === true && selection !== undefined;
  const visibleIds = (data?.content ?? []).map(api.rowKey);
  const allVisibleSelected =
    checkboxEnabled && visibleIds.length > 0
      && visibleIds.every((id) => selection!.isSelected(id));
  const someVisibleSelected =
    checkboxEnabled && visibleIds.some((id) => selection!.isSelected(id));

  const handleToggleAll = () => {
    if (!selection) return;
    if (allVisibleSelected) {
      selection.setIds(selection.selectedIds.filter((id) => !visibleIds.includes(id)));
    } else {
      const merged = Array.from(new Set([...selection.selectedIds, ...visibleIds]));
      selection.setIds(merged);
    }
  };

  return (
    <ListRoot>
      <ListSurface>
        <FilterBarArea>
          <ListSearchFilter
            searchFilter={searchFilter}
            filters={state.filters as Record<string, unknown>}
            onUpdate={(key, value) =>
              state.updateFilter(key as keyof TFilters, value as TFilters[keyof TFilters])
            }
            onReset={state.resetFilters}
            trailing={
              api.useExcel ? (
                <ExcelButton
                  useHook={api.useExcel}
                  filters={state.filters}
                  sort={state.sort}
                />
              ) : null
            }
          />
        </FilterBarArea>

        <ListTable<TRow>
          menuCode={api.menuCode}
          columns={column}
          rows={data?.content ?? []}
          rowKey={api.rowKey}
          page={state.page}
          pageSize={state.pageSize}
          sort={state.sort}
          onSortChange={state.setSort}
          isLoading={isFetching}
          emptyMessage={api.emptyMessage}
          onEdit={api.onEdit}
          onDelete={handleDelete}
          onRowClick={api.onRowClick}
          deleteConfirm={api.deleteConfirm}
          checkBox={checkboxEnabled}
          selection={selection}
          allVisibleSelected={allVisibleSelected}
          someVisibleSelected={someVisibleSelected}
          onToggleAll={handleToggleAll}
        />

        <ListPagination
          page={state.page}
          totalPages={data?.totalPages ?? 0}
          totalElements={data?.totalElements}
          onPageChange={state.setPage}
        />
      </ListSurface>
    </ListRoot>
  );
}

function ExcelButton<TFilters extends object>({
  useHook,
  filters,
  sort,
}: {
  useHook: UseExcelDownload<TFilters>;
  filters: TFilters;
  sort: SortState;
}) {
  const download = useHook();
  const handleClick = () => {
    download({
      ...filters,
      sort: `${sort.key},${sort.direction}`,
    });
  };
  return (
    <ExcelDownloadButton
      startIcon={<DownloadIcon />}
      onClick={handleClick}
      variant="outlined"
      size="small"
    >
      엑셀
    </ExcelDownloadButton>
  );
}
