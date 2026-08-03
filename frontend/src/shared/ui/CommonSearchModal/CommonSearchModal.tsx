import { useMemo } from 'react';
import Button from '@mui/material/Button';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import { ListPagination, ListSearchFilter } from '@/shared/ui/GenericList';
import { getErrorMessage } from '@/shared/api/error';
import {
  modalStateResetKey,
  useResettableState,
} from '@/shared/hooks/useResettableState';
import ModalShell from './ModalShell';
import SearchTable from './SearchTable';
import SelectionTray from './SelectionTray';
import { ModalFilterArea, ModalFixedRow } from './CommonSearchModal.styles';
import { useSearchModalQueryState } from './useSearchModalQueryState';
import type { CommonSearchModalProps } from './types';

/**
 * 행을 선택해 부모로 반환하는 검색 모달.
 * - 단일 / 다중 선택, 'checkbox' / 'tray' 시각 패턴.
 * - 확인 버튼으로 선택 결과를 부모에 전달, 취소 / 닫기로 폐기.
 *
 * 행 단위 액션 (삭제 등) 만 필요한 경우는 `CommonManageModal` 사용.
 */
export default function CommonSearchModal<TRow, TFilters extends object>({
  open,
  onClose,
  title,
  api,
  fixedQueryParams,
  scopeKey,
  searchFilter,
  column,
  emptyMessage,
  hidePagination,
  headerActions,
  multiple = false,
  selectionStyle = 'checkbox',
  onSelect,
  initialSelected = [],
  excludeIds,
  confirmLabel = '확인',
  renderTrayItem,
}: CommonSearchModalProps<TRow, TFilters>) {
  const { state, query, visibleRows } = useSearchModalQueryState({
    api, searchFilter, column, excludeIds, fixedQueryParams, scopeKey,
  });
  const { data, isFetching, isError, error, refetch } = query;

  const isTray = selectionStyle === 'tray';
  const [selectedMap, setSelectedMap] = useResettableState(
    modalStateResetKey(open, scopeKey),
    () => new Map(initialSelected.map((item) => [item.id, item])),
  );

  const isSelected = (id: number) => selectedMap.has(id);

  const toggleSelect = (row: TRow) => {
    const id = api.rowKey(row);
    const label = api.rowLabel ? api.rowLabel(row) : String(id);
    setSelectedMap((prev) => {
      const next = new Map(prev);
      if (multiple) {
        if (next.has(id)) next.delete(id);
        else next.set(id, { id, label });
      } else {
        next.clear();
        next.set(id, { id, label });
      }
      return next;
    });
  };

  const removeFromTray = (id: number) => {
    setSelectedMap((prev) => {
      if (!prev.has(id)) return prev;
      const next = new Map(prev);
      next.delete(id);
      return next;
    });
  };

  const handleConfirm = () => {
    onSelect(Array.from(selectedMap.values()));
    onClose();
  };

  const selectedItems = useMemo(() => Array.from(selectedMap.values()), [selectedMap]);
  const selectedCount = selectedMap.size;
  const hasFilter = !!searchFilter && searchFilter.length > 0;

  const footer = (
    <>
      <Button onClick={onClose}>취소</Button>
      <Button
        variant="contained"
        onClick={handleConfirm}
        disabled={selectedCount === 0}
        disableElevation
      >
        {confirmLabel}{selectedCount > 0 ? ` (${selectedCount})` : ''}
      </Button>
    </>
  );

  return (
    <ModalShell
      open={open}
      onClose={onClose}
      title={title}
      headerActions={headerActions}
      footer={footer}
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
          mode="select"
          selectionStyle={selectionStyle}
          isSelected={isSelected}
          onToggleSelect={toggleSelect}
          multiple={multiple}
          isLoading={isFetching}
          emptyMessage={emptyMessage ?? '결과가 없습니다.'}
          page={state.page}
          pageSize={state.pageSize}
        />
      )}

      {isTray && (
        <ModalFixedRow>
          <SelectionTray items={selectedItems} onRemove={removeFromTray} renderItem={renderTrayItem} />
        </ModalFixedRow>
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
