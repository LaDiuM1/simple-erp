import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AccountTreeRoundedIcon from '@mui/icons-material/AccountTreeRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import GenericList, { useListSelection } from '@/shared/ui/GenericList';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useDeleteDepartmentMutation } from '@/features/department/api/departmentApi';
import {
  departmentColumn,
  departmentSearchFilter,
  useDepartmentListApi,
} from '@/features/department/config/departmentListConfig';
import type { ApiError } from '@/shared/types/api';

export default function DepartmentListPage() {
  const navigate = useNavigate();
  const api = useDepartmentListApi();
  const selection = useListSelection();
  const snackbar = useSnackbar();
  const [deleteDepartment] = useDeleteDepartmentMutation();
  const [bulkConfirmOpen, setBulkConfirmOpen] = useState(false);
  const [isBulkDeleting, setIsBulkDeleting] = useState(false);

  const selectedCount = selection.selectedIds.length;

  const handleBulkDelete = async () => {
    setIsBulkDeleting(true);
    setBulkConfirmOpen(false);
    try {
      await Promise.all(
        selection.selectedIds.map((id) => deleteDepartment(id).unwrap()),
      );
      snackbar.success(`${selectedCount}건이 삭제되었습니다.`);
      selection.clear();
    } catch (err) {
      snackbar.error((err as ApiError)?.message ?? '일괄 삭제 중 오류가 발생했습니다.');
    } finally {
      setIsBulkDeleting(false);
    }
  };

  return (
    <>
      <PageHeaderActions
        actions={[
          ...(selectedCount > 0
            ? [
                {
                  design: 'delete' as const,
                  label: `선택 ${selectedCount}건 삭제`,
                  onClick: () => setBulkConfirmOpen(true),
                  loading: isBulkDeleting,
                  menuCode: MENU_CODE.DEPARTMENTS,
                },
              ]
            : []),
          {
            design: 'cancel' as const,
            label: '부서 계층 관리',
            icon: <AccountTreeRoundedIcon />,
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.DEPARTMENTS]}/hierarchy`),
          },
          {
            design: 'create' as const,
            label: '부서 등록',
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.DEPARTMENTS]}/new`),
            menuCode: MENU_CODE.DEPARTMENTS,
          },
        ]}
      />

      <GenericList
        api={api}
        searchFilter={departmentSearchFilter}
        column={departmentColumn}
        selection={selection}
      />

      <ConfirmModal
        isOpen={bulkConfirmOpen}
        title="선택 항목 삭제"
        message={`선택한 ${selectedCount}건을 삭제하시겠습니까?`}
        confirmLabel={isBulkDeleting ? '삭제 중...' : '삭제'}
        danger
        onConfirm={handleBulkDelete}
        onCancel={() => setBulkConfirmOpen(false)}
      />
    </>
  );
}
