import { useEffect, useMemo, useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogTitle from '@mui/material/DialogTitle';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import {
  ListPagination,
  ListSearchFilter,
  useListState,
} from '@/shared/ui/GenericList';
import type { ApiError } from '@/shared/types/api';
import SearchTable from './SearchTable';
import { ModalContent, ModalFilterArea } from './CommonSearchModal.styles';
import type { CommonSearchModalProps, CommonSearchSelectedItem } from './types';

/**
 * 도메인 검색 모달 — 설정 구조를 받아 특정 도메인을 검색해서 (id, label) 을 부모로 반환.
 * GenericList 의 building blocks (ListSearchFilter / useListState / ListPagination) 를 재사용.
 *
 * 다중 선택 (multiple=true) 시 페이지를 넘나들며 선택 상태가 보존되도록 Map 으로 관리.
 */
export default function CommonSearchModal<TRow, TFilters extends object>({
  open,
  onClose,
  title,
  multiple = false,
  api,
  searchFilter,
  column,
  onSelect,
  initialSelected = [],
  excludeIds,
  emptyMessage,
}: CommonSearchModalProps<TRow, TFilters>) {
  const [selectedMap, setSelectedMap] = useState<Map<number, CommonSearchSelectedItem>>(
    () => new Map(),
  );

  // 모달이 열릴 때마다 initialSelected 로 선택 상태 리셋.
  useEffect(() => {
    if (open) {
      setSelectedMap(new Map(initialSelected.map((x) => [x.id, x])));
    }
  }, [open, initialSelected]);

  const state = useListState<TFilters>({
    searchFilter,
    column,
    pageSize: api.pageSize ?? 5,
  });
  const query = api.useList(state.queryParams);
  const { data, isFetching, isError, error, refetch } = query;

  const visibleRows = useMemo(() => {
    const rows = data?.content ?? [];
    if (!excludeIds || excludeIds.length === 0) return rows;
    const excludeSet = new Set(excludeIds);
    return rows.filter((row) => !excludeSet.has(api.rowKey(row)));
  }, [data, excludeIds, api]);

  const isSelected = (id: number) => selectedMap.has(id);

  const toggleSelect = (row: TRow) => {
    const id = api.rowKey(row);
    const label = api.rowLabel(row);
    setSelectedMap((prev) => {
      const next = new Map(prev);
      if (multiple) {
        if (next.has(id)) next.delete(id);
        else next.set(id, { id, label });
      } else {
        // 단일 선택 — 기존 선택을 모두 비우고 새 항목만 유지.
        next.clear();
        next.set(id, { id, label });
      }
      return next;
    });
  };

  const handleConfirm = () => {
    onSelect(Array.from(selectedMap.values()));
    onClose();
  };

  const selectedCount = selectedMap.size;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>{title}</DialogTitle>
      <ModalContent>
        <ModalFilterArea>
          <ListSearchFilter
            searchFilter={searchFilter}
            filters={state.filters as Record<string, unknown>}
            onUpdate={(key, value) =>
              state.updateFilter(key as keyof TFilters, value as TFilters[keyof TFilters])
            }
            onReset={state.resetFilters}
          />
        </ModalFilterArea>

        {isError ? (
          <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />
        ) : (
          <SearchTable
            rows={visibleRows}
            columns={column}
            rowKey={api.rowKey}
            isSelected={isSelected}
            onToggleSelect={toggleSelect}
            multiple={multiple}
            isLoading={isFetching}
            emptyMessage={emptyMessage ?? '검색 결과가 없습니다.'}
            page={state.page}
            pageSize={state.pageSize}
          />
        )}

        <ListPagination
          page={state.page}
          totalPages={data?.totalPages ?? 0}
          totalElements={data?.totalElements}
          onPageChange={state.setPage}
        />
      </ModalContent>
      <DialogActions>
        <Button onClick={onClose}>취소</Button>
        <Button
          variant="contained"
          onClick={handleConfirm}
          disabled={selectedCount === 0}
          disableElevation
        >
          확인{selectedCount > 0 ? ` (${selectedCount})` : ''}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
