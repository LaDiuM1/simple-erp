import { type DragEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { getErrorMessage } from '@/shared/api/error';
import {
  useCreateProductCategoryMutation,
  useDeleteProductCategoryMutation,
  useGetProductCategoriesQuery,
  useReorderProductCategoriesMutation,
  useUpdateProductCategoryMutation,
} from '@/features/product/api/productApi';
import type { ProductCategorySummary } from '@/features/product/types';

const EMPTY_CATEGORIES: ProductCategorySummary[] = [];

/**
 * 제품 카테고리 관리 page hook — server list 동기화 + DnD 순서 변경 + 추가 / 이름 변경 / 삭제.
 * 직책 서열 관리 (usePositionRankingPage) 의 DnD 패턴을 따르고, CRUD 는 인라인 편집으로 처리.
 */
export function useProductCategoryPage() {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const { canWrite } = usePermission(MENU_CODE.PRODUCTS);
  const listQuery = useGetProductCategoriesQuery();
  const [createCategory, { isLoading: isCreating }] = useCreateProductCategoryMutation();
  const [updateCategory] = useUpdateProductCategoryMutation();
  const [deleteCategory, { isLoading: isDeleting }] = useDeleteProductCategoryMutation();
  const [reorderCategories] = useReorderProductCategoriesMutation();

  const serverItems = listQuery.data ?? EMPTY_CATEGORIES;
  const [items, setItems] = useState<ProductCategorySummary[]>(serverItems);
  const [itemsSource, setItemsSource] = useState(serverItems);
  if (itemsSource !== serverItems) {
    setItemsSource(serverItems);
    setItems(serverItems);
  }

  // --- 추가 ---
  const [newName, setNewName] = useState('');

  const handleCreate = async () => {
    const name = newName.trim();
    if (!name) return;
    try {
      await createCategory({ name }).unwrap();
      setNewName('');
      snackbar.success('카테고리가 추가되었습니다.');
    } catch (err) {
      snackbar.error(getErrorMessage(err, '카테고리 추가 중 오류가 발생했습니다.'));
    }
  };

  // --- 이름 변경 (인라인 편집) ---
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editingName, setEditingName] = useState('');

  const startEdit = (item: ProductCategorySummary) => {
    setEditingId(item.id);
    setEditingName(item.name);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditingName('');
  };

  const handleRename = async () => {
    if (editingId == null) return;
    const name = editingName.trim();
    const original = items.find((it) => it.id === editingId);
    if (!name || !original) return;
    if (name === original.name) {
      cancelEdit();
      return;
    }
    try {
      await updateCategory({ id: editingId, body: { name } }).unwrap();
      cancelEdit();
      snackbar.success('카테고리명이 변경되었습니다.');
    } catch (err) {
      snackbar.error(getErrorMessage(err, '카테고리명 변경 중 오류가 발생했습니다.'));
    }
  };

  // --- 삭제 (ConfirmModal 은 페이지가 렌더) ---
  const [deleteTarget, setDeleteTarget] = useState<ProductCategorySummary | null>(null);

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await deleteCategory(deleteTarget.id).unwrap();
      setDeleteTarget(null);
      snackbar.success('카테고리가 삭제되었습니다.');
    } catch (err) {
      setDeleteTarget(null);
      snackbar.error(getErrorMessage(err, '카테고리 삭제 중 오류가 발생했습니다.'));
    }
  };

  // --- DnD 순서 변경 ---
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
      await reorderCategories({ orderedIds: next.map((it) => it.id) }).unwrap();
      snackbar.success('카테고리 순서가 변경되었습니다.');
    } catch (err) {
      setItems(previous);
      snackbar.error(getErrorMessage(err, '순서 변경 중 오류가 발생했습니다.'));
    }
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      onClick: () => navigate(MENU_PATH[MENU_CODE.PRODUCTS]),
    },
  ];

  return {
    queries: { list: listQuery },
    items,
    canWrite,
    add: { value: newName, onChange: setNewName, onSubmit: handleCreate, isSubmitting: isCreating },
    edit: {
      editingId,
      editingName,
      onChangeName: setEditingName,
      onStart: startEdit,
      onCancel: cancelEdit,
      onSubmit: handleRename,
    },
    remove: {
      target: deleteTarget,
      onRequest: setDeleteTarget,
      onCancel: () => setDeleteTarget(null),
      onConfirm: handleDelete,
      isDeleting,
    },
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
