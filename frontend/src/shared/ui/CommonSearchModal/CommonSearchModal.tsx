import { useEffect, useMemo, useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import {
  ListPagination,
  ListSearchFilter,
  useListState,
} from '@/shared/ui/GenericList';
import { getErrorMessage } from '@/shared/api/error';
import SearchTable from './SearchTable';
import SelectionTray from './SelectionTray';
import { ModalContent, ModalFilterArea, ModalTitleRow } from './CommonSearchModal.styles';
import type { CommonSearchModalProps, CommonSearchSelectedItem } from './types';

/**
 * 도메인 검색 / 관리 공용 모달.
 * - select 모드 (default): 행 선택 → (id, label) 부모로 반환. 다중 선택 가능.
 *   - selectionStyle 'checkbox' (default): 행마다 체크박스.
 *   - selectionStyle 'tray': 행 클릭 → 하단 트레이에 칩 누적, 칩 X 로 제거.
 * - manage 모드: rowActions slot 으로 행별 액션 (예: 삭제). DialogActions 의 확인 대신 닫기 단일 버튼.
 *
 * 리스트 표현은 GenericList 의 데스크탑 Table / 모바일 카드 split 시각 톤을 따른다.
 */
export default function CommonSearchModal<TRow, TFilters extends object>(
  props: CommonSearchModalProps<TRow, TFilters>,
) {
  const {
    open,
    onClose,
    title,
    api,
    searchFilter,
    column,
    emptyMessage,
    hidePagination,
    headerActions,
  } = props;
  const isManage = props.mode === 'manage';
  const initialSelected = !isManage ? props.initialSelected ?? [] : [];
  const selectionStyle = !isManage ? props.selectionStyle ?? 'checkbox' : 'checkbox';
  const isTray = !isManage && selectionStyle === 'tray';
  const confirmLabel = !isManage ? props.confirmLabel ?? '확인' : '확인';
  const renderTrayItem = !isManage ? props.renderTrayItem : undefined;

  const [selectedMap, setSelectedMap] = useState<Map<number, CommonSearchSelectedItem>>(
    () => new Map(),
  );

  // 모달이 열릴 때마다 initialSelected 로 선택 상태 리셋 (select 모드 전용).
  useEffect(() => {
    if (open && !isManage) {
      setSelectedMap(new Map(initialSelected.map((x) => [x.id, x])));
    }
    // initialSelected 는 호출자가 매번 새 array 를 만들 수 있어 effect 의존성에서 제외.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, isManage]);

  const state = useListState<TFilters>({
    searchFilter: searchFilter ?? [],
    column,
    pageSize: api.pageSize ?? 5,
  });
  const query = api.useList(state.queryParams);
  const { data, isFetching, isError, error, refetch } = query;

  const visibleRows = useMemo(() => {
    const rows = data?.content ?? [];
    if (isManage) return rows;
    const excludeIds = props.excludeIds;
    if (!excludeIds || excludeIds.length === 0) return rows;
    const excludeSet = new Set(excludeIds);
    return rows.filter((row) => !excludeSet.has(api.rowKey(row)));
  }, [data, api, isManage, props]);

  const isSelected = (id: number) => selectedMap.has(id);

  const toggleSelect = (row: TRow) => {
    if (isManage) return;
    const id = api.rowKey(row);
    const label = api.rowLabel ? api.rowLabel(row) : String(id);
    const multiple = props.multiple ?? false;
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
    if (isManage) return;
    props.onSelect(Array.from(selectedMap.values()));
    onClose();
  };

  const selectedCount = selectedMap.size;
  const selectedItems = useMemo(() => Array.from(selectedMap.values()), [selectedMap]);
  const hasFilter = !!searchFilter && searchFilter.length > 0;

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="xl"
      fullWidth
      slotProps={{ paper: { sx: { height: '85vh' } } }}
    >
      <ModalTitleRow>
        <span>{title}</span>
        {headerActions && <span>{headerActions}</span>}
      </ModalTitleRow>
      <ModalContent>
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
            mode={isManage ? 'manage' : 'select'}
            selectionStyle={selectionStyle}
            isSelected={isSelected}
            onToggleSelect={toggleSelect}
            multiple={!isManage && (props.multiple ?? false)}
            rowActions={isManage ? props.rowActions : undefined}
            isLoading={isFetching}
            emptyMessage={emptyMessage ?? '결과가 없습니다.'}
            page={state.page}
            pageSize={state.pageSize}
          />
        )}

        {isTray && (
          <SelectionTray items={selectedItems} onRemove={removeFromTray} renderItem={renderTrayItem} />
        )}

        {!hidePagination && (
          <ListPagination
            page={state.page}
            totalPages={data?.totalPages ?? 0}
            totalElements={data?.totalElements}
            onPageChange={state.setPage}
          />
        )}
      </ModalContent>
      <DialogActions>
        <Button onClick={onClose}>{isManage ? '닫기' : '취소'}</Button>
        {!isManage && (
          <Button
            variant="contained"
            onClick={handleConfirm}
            disabled={selectedCount === 0}
            disableElevation
          >
            {confirmLabel}{selectedCount > 0 ? ` (${selectedCount})` : ''}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}
