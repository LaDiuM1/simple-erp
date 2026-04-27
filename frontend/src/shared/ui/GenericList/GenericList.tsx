import { useState } from 'react';
import DeleteIcon from '@mui/icons-material/DeleteOutline';
import DownloadIcon from '@mui/icons-material/FileDownloadOutlined';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { usePermission } from '@/shared/hooks/usePermission';
import { getErrorMessage } from '@/shared/api/error';
import ListSearchFilter from './ListSearchFilter';
import ListTable from './ListTable';
import ListPagination from './ListPagination';
import { useListState } from './useListState';
import { useListSelection } from './useListSelection';
import {
  BulkDeleteButton,
  ExcelDownloadButton,
  FilterBarArea,
  FilterBarTrailing,
  ListRoot,
  ListSurface,
} from './GenericList.styles';
import type {
  ColumnConfig,
  DeleteConfirmMessages,
  FilterConfig,
  ListApiConfig,
  SortState,
  UseBulkDeleteMutation,
  UseDeleteMutation,
  UseExcelDownload,
} from './types';

const DEFAULT_DELETE_SUCCESS = '삭제되었습니다.';
const DEFAULT_DELETE_ERROR = '삭제 중 오류가 발생했습니다.';
const DEFAULT_BULK_DELETE_CONFIRM: DeleteConfirmMessages = {
  title: '선택 항목 삭제',
  message: '선택한 {count}건을 삭제하시겠습니까?',
};

/**
 * api.useDelete 가 미지정인 페이지에서 hooks 호출 순서 유지용 placeholder.
 * 실제 deleteFn 은 호출되지 않는다 (handleDelete 가 undefined 로 전달되어 ListTable 이 모바일 행 삭제 아이콘을 그리지 않음).
 */
const noopDeleteHook: UseDeleteMutation = () =>
  [() => ({ unwrap: async () => undefined }), undefined] as const;

/** api.useBulkDelete 가 미지정인 페이지에서 hooks 호출 순서 유지용 placeholder. */
const noopBulkDeleteHook: UseBulkDeleteMutation = () =>
  [() => ({ unwrap: async () => undefined }), undefined] as const;

export interface GenericListProps<TRow, TFilters extends object> {
  api: ListApiConfig<TRow, TFilters>;
  searchFilter: FilterConfig[];
  column: ColumnConfig<TRow>[];
}

/**
 * 도메인 목록 본문 — 검색바 / 테이블 / 페이지네이션 / 일괄 삭제 / 엑셀 다운로드까지 합성.
 * 페이지 헤더는 GenericList 밖에서 페이지가 직접 조립한다.
 *
 * 데스크탑: 행 클릭 → 상세 페이지 + 체크박스 일괄 삭제 (api.useBulkDelete 지정 시).
 * 모바일: 카드 + 행별 수정/삭제 아이콘 (api.onEdit / api.useDelete 지정 시).
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
  const selection = useListSelection();
  const { canWrite } = usePermission(api.menuCode);

  const query = api.useList(state.queryParams);
  const { data, isLoading, isFetching, isError, error, refetch } = query;

  // useDelete (모바일 단건) 가 없는 페이지는 noopDeleteHook 으로 hooks 순서 유지 + onDelete 미전달.
  const useDeleteHook = api.useDelete ?? noopDeleteHook;
  const [deleteFn] = useDeleteHook();

  const handleDelete = api.useDelete
    ? async (row: TRow) => {
        try {
          const id = api.rowKey(row);
          await deleteFn(id).unwrap();
          snackbar.success(api.successMessages?.delete ?? DEFAULT_DELETE_SUCCESS);
          if (selection.isSelected(id)) {
            selection.toggle(id);
          }
        } catch (err) {
          snackbar.error(getErrorMessage(err, DEFAULT_DELETE_ERROR));
          throw err;
        }
      }
    : undefined;

  // useBulkDelete 가 지정된 경우에만 데스크탑 체크박스 + 일괄 삭제 버튼 활성화.
  const useBulkDeleteHook = api.useBulkDelete ?? noopBulkDeleteHook;
  const [bulkDeleteFn] = useBulkDeleteHook();
  const [bulkConfirmOpen, setBulkConfirmOpen] = useState(false);
  const [isBulkDeleting, setIsBulkDeleting] = useState(false);

  const checkboxEnabled = !!api.useBulkDelete && canWrite;
  const visibleIds = (data?.content ?? []).map(api.rowKey);
  const allVisibleSelected =
    checkboxEnabled && visibleIds.length > 0
      && visibleIds.every((id) => selection.isSelected(id));
  const someVisibleSelected =
    checkboxEnabled && visibleIds.some((id) => selection.isSelected(id));

  const handleToggleAll = () => {
    if (allVisibleSelected) {
      selection.setIds(selection.selectedIds.filter((id) => !visibleIds.includes(id)));
    } else {
      const merged = Array.from(new Set([...selection.selectedIds, ...visibleIds]));
      selection.setIds(merged);
    }
  };

  const selectedCount = selection.selectedIds.length;

  const handleBulkDelete = async () => {
    if (!api.useBulkDelete || selectedCount === 0) return;
    setIsBulkDeleting(true);
    try {
      await bulkDeleteFn(selection.selectedIds).unwrap();
      const tmpl = api.successMessages?.bulkDelete ?? `${selectedCount}건이 삭제되었습니다.`;
      snackbar.success(tmpl.replace('{count}', String(selectedCount)));
      selection.clear();
      setBulkConfirmOpen(false);
    } catch (err) {
      snackbar.error(getErrorMessage(err, DEFAULT_DELETE_ERROR));
    } finally {
      setIsBulkDeleting(false);
    }
  };

  if (isError) {
    return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;
  }

  const bulkConfirm = { ...DEFAULT_BULK_DELETE_CONFIRM, ...api.bulkDeleteConfirm };
  const bulkConfirmMessage = bulkConfirm.message.replace('{count}', String(selectedCount));

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
              (checkboxEnabled && selectedCount > 0) || api.useExcel ? (
                <FilterBarTrailing>
                  {checkboxEnabled && selectedCount > 0 && (
                    <BulkDeleteButton
                      startIcon={<DeleteIcon />}
                      onClick={() => setBulkConfirmOpen(true)}
                      variant="outlined"
                      size="small"
                      disabled={isBulkDeleting}
                    >
                      선택 {selectedCount}건 삭제
                    </BulkDeleteButton>
                  )}
                  {api.useExcel && (
                    <ExcelButton
                      useHook={api.useExcel}
                      filters={state.filters}
                      sort={state.sort}
                    />
                  )}
                </FilterBarTrailing>
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
          isLoading={isLoading}
          isFetching={isFetching}
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

      {api.useBulkDelete && (
        <ConfirmModal
          isOpen={bulkConfirmOpen}
          title={bulkConfirm.title}
          message={bulkConfirmMessage}
          confirmLabel={isBulkDeleting ? '삭제 중...' : '삭제'}
          danger
          onConfirm={handleBulkDelete}
          onCancel={() => setBulkConfirmOpen(false)}
        />
      )}
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
