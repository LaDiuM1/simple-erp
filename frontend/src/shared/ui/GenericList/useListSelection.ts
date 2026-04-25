import { useCallback, useState } from 'react';

/** GenericList 의 행 선택 상태 — 페이지가 소유하고 GenericList 와 일괄 액션 버튼이 함께 참조. */
export interface ListSelectionState {
  selectedIds: number[];
  isSelected: (id: number) => boolean;
  toggle: (id: number) => void;
  setIds: (ids: number[]) => void;
  clear: () => void;
}

export function useListSelection(): ListSelectionState {
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  const isSelected = useCallback(
    (id: number) => selectedIds.includes(id),
    [selectedIds],
  );

  const toggle = useCallback((id: number) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id],
    );
  }, []);

  const setIds = useCallback((ids: number[]) => {
    setSelectedIds(ids);
  }, []);

  const clear = useCallback(() => {
    setSelectedIds([]);
  }, []);

  return { selectedIds, isSelected, toggle, setIds, clear };
}
