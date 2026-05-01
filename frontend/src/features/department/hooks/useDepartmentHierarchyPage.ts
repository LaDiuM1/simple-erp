import { useMemo, useState, type DragEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useUpdateDepartmentMutation } from '@/features/department/api/departmentApi';
import { useGetDepartmentsQuery } from '@/features/reference/api/referenceApi';
import {
  ROOT_KEY,
  buildChildrenMap,
  isDescendant,
  type DragOverKey,
} from '@/features/department/components/HierarchyTree/HierarchyTree';
import { getErrorMessage } from '@/shared/api/error';

/**
 * 부서 계층 page hook — query + DnD state + reparent mutation + headerActions.
 * 캐시된 reference 부서 목록을 parentId 기준으로 트리 구성. cycle 방지 로직 포함.
 */
export function useDepartmentHierarchyPage() {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const { canWrite } = usePermission(MENU_CODE.DEPARTMENTS);
  const listQuery = useGetDepartmentsQuery();
  const [updateDepartment] = useUpdateDepartmentMutation();

  const [draggedId, setDraggedId] = useState<number | null>(null);
  const [dragOverKey, setDragOverKey] = useState<DragOverKey>(null);

  const depts = listQuery.data ?? [];
  const childrenByParent = useMemo(() => buildChildrenMap(depts), [depts]);
  const rootDepts = childrenByParent.get(null) ?? [];

  const move = async (sourceId: number, newParentId: number | null) => {
    const dragged = depts.find((d) => d.id === sourceId);
    if (!dragged) return;

    if (newParentId === sourceId) return;
    if (newParentId !== null && isDescendant(sourceId, newParentId, childrenByParent)) {
      snackbar.error('하위 부서를 상위 부서로 설정할 수 없습니다.');
      return;
    }
    if ((dragged.parentId ?? null) === newParentId) return;

    try {
      await updateDepartment({
        id: sourceId,
        body: { name: dragged.name, parentId: newParentId },
      }).unwrap();
      snackbar.success('부서 계층이 변경되었습니다.');
    } catch (err) {
      snackbar.error(getErrorMessage(err, '계층 변경 중 오류가 발생했습니다.'));
    }
  };

  const handleDragStart = (e: DragEvent, id: number) => {
    if (!canWrite) return;
    e.dataTransfer.effectAllowed = 'move';
    setDraggedId(id);
  };

  const handleDragEnd = () => {
    setDraggedId(null);
    setDragOverKey(null);
  };

  const handleDragOver = (e: DragEvent, key: number | typeof ROOT_KEY) => {
    if (!canWrite || draggedId == null) return;
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    setDragOverKey(key);
  };

  const handleDragLeave = () => {
    setDragOverKey(null);
  };

  const handleDrop = (targetParentId: number | null) => {
    setDragOverKey(null);
    if (draggedId == null) return;
    void move(draggedId, targetParentId);
    setDraggedId(null);
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      onClick: () => navigate(MENU_PATH[MENU_CODE.DEPARTMENTS]),
    },
  ];

  return {
    queries: { list: listQuery },
    rootDepts,
    childrenByParent,
    canWrite,
    draggedId,
    dragOverKey,
    onDragStart: handleDragStart,
    onDragEnd: handleDragEnd,
    onDragOver: handleDragOver,
    onDragLeave: handleDragLeave,
    onDrop: handleDrop,
    headerActions,
  };
}
