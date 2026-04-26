import { useState } from 'react';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import EventNoteRoundedIcon from '@mui/icons-material/EventNoteRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import { usePermission } from '@/shared/hooks/usePermission';
import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { SecondaryPageHeaderButton } from '@/shared/ui/layout/PageHeaderButton';
import Muted from '@/shared/ui/atoms/Muted';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useDeleteSalesActivityMutation } from '@/features/salesCustomer/api/salesCustomerApi';
import type { SalesActivity } from '@/features/salesCustomer/types';
import { getErrorMessage } from '@/shared/api/error';
import ActivityTypeBadge from './ActivityTypeBadge';
import ActivityFormModal from './ActivityFormModal/ActivityFormModal';
import {
  EmptySection,
  ItemCard,
  ItemContent,
  ItemHeader,
  ItemHeaderLeft,
  ItemHeaderRight,
  ItemList,
  ItemSubtle,
  ItemTitle,
  SectionHeader,
  SectionRoot,
  SectionTitle,
  SectionTitleCount,
} from './salesCustomerDetail.styles';

interface Props {
  customerId: number;
  activities: SalesActivity[];
}

export default function ActivityList({ customerId, activities }: Props) {
  const { canWrite } = usePermission(MENU_CODE.SALES_CUSTOMERS);
  const snackbar = useSnackbar();
  const [deleteMut, { isLoading: isDeleting }] = useDeleteSalesActivityMutation();

  const [editing, setEditing] = useState<SalesActivity | null>(null);
  const [creating, setCreating] = useState(false);
  const [deletingTarget, setDeletingTarget] = useState<SalesActivity | null>(null);

  const handleDelete = async () => {
    if (!deletingTarget) return;
    try {
      await deleteMut({ id: deletingTarget.id, customerId }).unwrap();
      snackbar.success('활동이 삭제되었습니다.');
      setDeletingTarget(null);
    } catch (err) {
      snackbar.error(getErrorMessage(err, '삭제 중 오류가 발생했습니다.'));
    }
  };

  return (
    <SectionRoot>
      <SectionHeader>
        <SectionTitle>
          <EventNoteRoundedIcon sx={{ fontSize: 18, color: 'primary.main' }} />
          영업 활동
          <SectionTitleCount>({activities.length})</SectionTitleCount>
        </SectionTitle>
        {canWrite && (
          <SecondaryPageHeaderButton
            startIcon={<AddRoundedIcon sx={{ fontSize: 18 }} />}
            onClick={() => setCreating(true)}
          >
            활동 등록
          </SecondaryPageHeaderButton>
        )}
      </SectionHeader>

      {activities.length === 0 ? (
        <EmptySection>등록된 영업 활동이 없습니다.</EmptySection>
      ) : (
        <ItemList>
          {activities.map((a) => (
            <ItemCard key={a.id}>
              <ItemHeader>
                <ItemHeaderLeft>
                  <ActivityTypeBadge type={a.type} />
                  <ItemTitle>{a.subject}</ItemTitle>
                  <ItemSubtle>{formatDateTime(a.activityDate)}</ItemSubtle>
                </ItemHeaderLeft>
                {canWrite && (
                  <ItemHeaderRight>
                    <Tooltip title="수정" arrow>
                      <IconButton size="small" onClick={() => setEditing(a)}>
                        <EditOutlinedIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title="삭제" arrow>
                      <IconButton
                        size="small"
                        onClick={() => setDeletingTarget(a)}
                        sx={{ '&:hover': { color: 'error.main' } }}
                      >
                        <DeleteOutlineIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </ItemHeaderRight>
                )}
              </ItemHeader>
              <ItemSubtle>
                우리 담당: {a.ourEmployeeName ?? <Muted />}
                {a.ourEmployeeDepartmentName ? ` · ${a.ourEmployeeDepartmentName}` : ''}
                {' · '}
                고객 담당: {a.customerContactRegisteredName ?? a.customerContactName ?? <Muted />}
                {a.customerContactPosition ? ` (${a.customerContactPosition})` : ''}
              </ItemSubtle>
              {a.content && <ItemContent>{a.content}</ItemContent>}
            </ItemCard>
          ))}
        </ItemList>
      )}

      <ActivityFormModal
        open={creating}
        onClose={() => setCreating(false)}
        customerId={customerId}
      />
      <ActivityFormModal
        open={editing !== null}
        onClose={() => setEditing(null)}
        customerId={customerId}
        activity={editing ?? undefined}
      />
      <ConfirmModal
        isOpen={deletingTarget !== null}
        title="영업 활동 삭제"
        message={`"${deletingTarget?.subject ?? ''}" 활동을 삭제하시겠습니까?`}
        confirmLabel={isDeleting ? '삭제 중...' : '삭제'}
        danger
        onConfirm={handleDelete}
        onCancel={() => setDeletingTarget(null)}
      />
    </SectionRoot>
  );
}

function formatDateTime(iso: string): string {
  return iso.replace('T', ' ').slice(0, 16);
}
