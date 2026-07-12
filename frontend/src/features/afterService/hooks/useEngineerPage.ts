import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { getErrorMessage } from '@/shared/api/error';
import {
  useDeleteEngineerMutation,
  useGetEngineersQuery,
} from '@/features/afterService/api/afterServiceApi';
import type { Engineer } from '@/features/afterService/types';

/**
 * 엔지니어 관리 page hook — AS 관리 (AFTER_SERVICES) 의 서브 기능.
 * 목록 조회 + 등록 / 수정 모달 state + 삭제 (참조 중 거부는 BE 가 판단).
 */
export function useEngineerPage() {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const { canWrite } = usePermission(MENU_CODE.AFTER_SERVICES);
  const listQuery = useGetEngineersQuery();
  const [deleteEngineer, { isLoading: isDeleting }] = useDeleteEngineerMutation();

  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<Engineer | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Engineer | null>(null);

  const handleDelete = async () => {
    if (!deleteTarget) return;
    try {
      await deleteEngineer(deleteTarget.id).unwrap();
      setDeleteTarget(null);
      snackbar.success('엔지니어가 삭제되었습니다.');
    } catch (err) {
      setDeleteTarget(null);
      snackbar.error(getErrorMessage(err, '엔지니어 삭제 중 오류가 발생했습니다.'));
    }
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      onClick: () => navigate(MENU_PATH[MENU_CODE.AFTER_SERVICES]),
    },
    ...(canWrite
      ? [
          {
            design: 'create' as const,
            label: '엔지니어 등록',
            onClick: () => setCreating(true),
            menuCode: MENU_CODE.AFTER_SERVICES,
          },
        ]
      : []),
  ];

  return {
    queries: { engineers: listQuery },
    canWrite,
    headerActions,
    modal: {
      creating,
      editing,
      onCloseCreate: () => setCreating(false),
      onCloseEdit: () => setEditing(null),
    },
    onRowClick: (engineer: Engineer) => {
      if (canWrite) setEditing(engineer);
    },
    remove: {
      target: deleteTarget,
      onRequest: setDeleteTarget,
      onCancel: () => setDeleteTarget(null),
      onConfirm: handleDelete,
      isDeleting,
    },
  };
}
