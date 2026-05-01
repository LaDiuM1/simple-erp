import { type DragEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  useGetPositionsRankingQuery,
  useReorderPositionsMutation,
} from '@/features/position/api/positionApi';
import type { PositionSummary } from '@/features/position/types';
import { getErrorMessage } from '@/shared/api/error';

/**
 * 직책 서열 page hook — server list 동기화 + DnD state + reorder mutation + headerActions.
 *
 * server→local mirror 는 useEffect 로 동기화 (mutation 성공 후 refetch 가 갱신).
 * 드래그 중 외부 cache 무효화로 jump 가능성은 edge case — 수용.
 */
export function usePositionRankingPage() {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const { canWrite } = usePermission(MENU_CODE.POSITIONS);
  const listQuery = useGetPositionsRankingQuery();
  const [reorderPositions] = useReorderPositionsMutation();

  const [items, setItems] = useState<PositionSummary[]>([]);
  useEffect(() => {
    setItems(listQuery.data ?? []);
  }, [listQuery.data]);

  const [draggedId, setDraggedId] = useState<number | null>(null);
  const [dragOverId, setDragOverId] = useState<number | null>(null);

  const handleDragStart = (e: DragEvent, id: number) => {
    if (!canWrite) return;
    e.dataTransfer.effectAllowed = 'move';
    setDraggedId(id);
  };

  const handleDragEnd = () => {
    setDraggedId(null);
    setDragOverId(null);
  };

  const handleDragOver = (e: DragEvent, id: number) => {
    if (!canWrite || draggedId == null) return;
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    if (id !== dragOverId) setDragOverId(id);
  };

  const handleDragLeave = () => {
    setDragOverId(null);
  };

  const handleDrop = async (targetId: number) => {
    setDragOverId(null);
    const sourceId = draggedId;
    setDraggedId(null);

    if (sourceId == null || sourceId === targetId) return;

    const fromIndex = items.findIndex((it) => it.id === sourceId);
    const toIndex = items.findIndex((it) => it.id === targetId);
    if (fromIndex === -1 || toIndex === -1 || fromIndex === toIndex) return;

    const previous = items;
    const next = [...items];
    const [moved] = next.splice(fromIndex, 1);
    next.splice(toIndex, 0, moved);
    setItems(next);

    try {
      await reorderPositions({ orderedIds: next.map((it) => it.id) }).unwrap();
      snackbar.success('직책 서열이 변경되었습니다.');
    } catch (err) {
      setItems(previous);
      snackbar.error(getErrorMessage(err, '서열 변경 중 오류가 발생했습니다.'));
    }
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      onClick: () => navigate(MENU_PATH[MENU_CODE.POSITIONS]),
    },
  ];

  return {
    queries: { list: listQuery },
    items,
    canWrite,
    draggedId,
    dragOverId,
    onDragStart: handleDragStart,
    onDragEnd: handleDragEnd,
    onDragOver: handleDragOver,
    onDragLeave: handleDragLeave,
    onDrop: handleDrop,
    headerActions,
  };
}
