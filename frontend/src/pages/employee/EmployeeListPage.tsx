import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE } from '@/shared/config/menuConfig';
import GenericList, { useListSelection } from '@/shared/ui/GenericList';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useDeleteEmployeeMutation } from '@/features/employee/api/employeeApi';
import {
  employeeColumn,
  employeeSearchFilter,
  useEmployeeListApi,
} from '@/features/employee/config/employeeListConfig';
import type { ApiError } from '@/shared/types/api';

export default function EmployeeListPage() {
  const navigate = useNavigate();
  const api = useEmployeeListApi();
  const selection = useListSelection();
  const snackbar = useSnackbar();
  const [deleteEmployee] = useDeleteEmployeeMutation();
  const [bulkConfirmOpen, setBulkConfirmOpen] = useState(false);
  const [isBulkDeleting, setIsBulkDeleting] = useState(false);

  const selectedCount = selection.selectedIds.length;

  const handleBulkDelete = async () => {
    setIsBulkDeleting(true);
    setBulkConfirmOpen(false);
    try {
      await Promise.all(
        selection.selectedIds.map((id) => deleteEmployee(id).unwrap()),
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
                  menuCode: MENU_CODE.EMPLOYEES,
                },
              ]
            : []),
          {
            design: 'create' as const,
            label: '직원 등록',
            onClick: () => navigate('/employees/new'),
            menuCode: MENU_CODE.EMPLOYEES,
          },
        ]}
      />

      <GenericList
        api={api}
        searchFilter={employeeSearchFilter}
        column={employeeColumn}
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
