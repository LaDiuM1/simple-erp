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
  UseExcelDownload,
} from './types';

const DEFAULT_DELETE_SUCCESS = '삭제되었습니다.';
const DEFAULT_DELETE_ERROR = '삭제 중 오류가 발생했습니다.';

export interface GenericListProps<TRow, TFilters extends object> {
  api: ListApiConfig<TRow, TFilters>;
  searchFilter: FilterConfig[];
  column: ColumnConfig<TRow>[];
}

/**
 * 도메인 목록 본문 — 검색바 / 테이블 / 페이지네이션 / 행 삭제 확인 / 엑셀 다운로드까지 합성.
 * 페이지 헤더는 GenericList 밖에서 페이지가 직접 조립한다.
 */
export default function GenericList<TRow, TFilters extends object>({
  api,
  searchFilter,
  column,
}: GenericListProps<TRow, TFilters>) {
  const state = useListState<TFilters>({
    searchFilter,
    column,
    pageSize: api.pageSize,
  });
  const snackbar = useSnackbar();

  const query = api.useList(state.queryParams);
  const [deleteFn] = api.useDelete();
  const { data, isFetching, isError, error, refetch } = query;

  const handleDelete = async (row: TRow) => {
    try {
      await deleteFn(api.rowKey(row)).unwrap();
      snackbar.success(api.successMessages?.delete ?? DEFAULT_DELETE_SUCCESS);
    } catch (err) {
      snackbar.error((err as ApiError)?.message ?? DEFAULT_DELETE_ERROR);
      throw err;
    }
  };

  if (isError) {
    return <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />;
  }

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
          deleteConfirm={api.deleteConfirm}
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

/* --------------------------------------------------------------------------
 * Excel button — api.useExcel 이 있을 때만 렌더되므로 hook 은 unconditional.
 * ------------------------------------------------------------------------ */

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
