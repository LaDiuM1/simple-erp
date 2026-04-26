import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import LowPriorityRoundedIcon from '@mui/icons-material/LowPriorityRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import GenericList, { useListSelection } from '@/shared/ui/GenericList';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useDeletePositionMutation } from '@/features/position/api/positionApi';
import {
  positionColumn,
  positionSearchFilter,
  usePositionListApi,
} from '@/features/position/config/positionListConfig';
import { getErrorMessage } from '@/shared/api/error';

export default function PositionListPage() {
  const navigate = useNavigate();
  const api = usePositionListApi();
  const selection = useListSelection();
  const snackbar = useSnackbar();
  const [deletePosition] = useDeletePositionMutation();
  const [bulkConfirmOpen, setBulkConfirmOpen] = useState(false);
  const [isBulkDeleting, setIsBulkDeleting] = useState(false);

  const selectedCount = selection.selectedIds.length;

  const handleBulkDelete = async () => {
    setIsBulkDeleting(true);
    setBulkConfirmOpen(false);
    try {
      await Promise.all(
        selection.selectedIds.map((id) => deletePosition(id).unwrap()),
      );
      snackbar.success(`${selectedCount}건이 삭제되었습니다.`);
      selection.clear();
    } catch (err) {
      snackbar.error(getErrorMessage(err, '일괄 삭제 중 오류가 발생했습니다.'));
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
                  menuCode: MENU_CODE.POSITIONS,
                },
              ]
            : []),
          {
            design: 'cancel' as const,
            label: '직책 서열 관리',
            icon: <LowPriorityRoundedIcon />,
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.POSITIONS]}/ranking`),
          },
          {
            design: 'create' as const,
            label: '직책 등록',
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.POSITIONS]}/new`),
            menuCode: MENU_CODE.POSITIONS,
          },
        ]}
      />

      <GenericList
        api={api}
        searchFilter={positionSearchFilter}
        column={positionColumn}
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
