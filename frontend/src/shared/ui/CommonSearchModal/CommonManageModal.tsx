import Button from '@mui/material/Button';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import { ListPagination, ListSearchFilter } from '@/shared/ui/GenericList';
import { getErrorMessage } from '@/shared/api/error';
import ModalShell from './ModalShell';
import SearchTable from './SearchTable';
import { ModalFilterArea, ModalFixedRow } from './CommonSearchModal.styles';
import { useSearchModalQueryState } from './useSearchModalQueryState';
import type { CommonManageModalProps } from './types';

/**
 * 행 단위 액션 (예: 삭제) 만 노출하는 관리 모달.
 * 선택 / 확인 단계 없음 — 닫기 버튼만. 행 우측의 `rowActions` slot 으로 액션 IconButton 렌더.
 *
 * 행을 선택해 부모로 돌려보내는 흐름은 `CommonSearchModal` 사용.
 */
export default function CommonManageModal<TRow, TFilters extends object>({
  open,
  onClose,
  title,
  api,
  searchFilter,
  column,
  emptyMessage,
  hidePagination,
  headerActions,
  rowActions,
}: CommonManageModalProps<TRow, TFilters>) {
  const { state, query, visibleRows } = useSearchModalQueryState({
    api, searchFilter, column,
  });
  const { data, isFetching, isError, error, refetch } = query;

  const hasFilter = !!searchFilter && searchFilter.length > 0;

  return (
    <ModalShell
      open={open}
      onClose={onClose}
      title={title}
      headerActions={headerActions}
      footer={<Button onClick={onClose}>닫기</Button>}
    >
      {hasFilter && (
        <ModalFilterArea>
          <ListSearchFilter
            searchFilter={searchFilter!}
            filters={state.filters as Record<string, unknown>}
            onUpdate={(key, value) =>
              state.updateFilter(key as keyof TFilters, value as TFilters[keyof TFilters])
            }
            onReset={state.resetFilters}
          />
        </ModalFilterArea>
      )}

      {isError ? (
        <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />
      ) : (
        <SearchTable
          rows={visibleRows}
          columns={column}
          rowKey={api.rowKey}
          mode="manage"
          rowActions={rowActions}
          isLoading={isFetching}
          emptyMessage={emptyMessage ?? '결과가 없습니다.'}
          page={state.page}
          pageSize={state.pageSize}
        />
      )}

      {!hidePagination && (
        <ModalFixedRow>
          <ListPagination
            page={state.page}
            totalPages={data?.totalPages ?? 0}
            totalElements={data?.totalElements}
            onPageChange={state.setPage}
          />
        </ModalFixedRow>
      )}
    </ModalShell>
  );
}
