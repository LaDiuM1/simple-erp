import { useState } from 'react';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import WorkRoundedIcon from '@mui/icons-material/WorkRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import StopCircleOutlinedIcon from '@mui/icons-material/StopCircleOutlined';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import { usePermission } from '@/shared/hooks/usePermission';
import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { SecondaryPageHeaderButton } from '@/shared/ui/layout/PageHeaderButton';
import Muted from '@/shared/ui/atoms/Muted';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useDeleteSalesContactEmploymentMutation } from '@/features/salesContact/api/salesContactApi';
import type { SalesContactEmployment } from '@/features/salesContact/types';
import type { ApiError } from '@/shared/types/api';
import EmploymentStatusIndicator from './EmploymentStatusIndicator';
import EmploymentFormModal from './EmploymentFormModal/EmploymentFormModal';
import EmploymentTerminateModal from './EmploymentTerminateModal/EmploymentTerminateModal';
import {
  EmptySection,
  ItemCard,
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
} from './salesContactDetail.styles';

interface Props {
  contactId: number;
  employments: SalesContactEmployment[];
}

export default function EmploymentList({ contactId, employments }: Props) {
  const { canWrite } = usePermission(MENU_CODE.SALES_CONTACTS);
  const snackbar = useSnackbar();
  const [deleteMut, { isLoading: isDeleting }] = useDeleteSalesContactEmploymentMutation();

  const [editing, setEditing] = useState<SalesContactEmployment | null>(null);
  const [creating, setCreating] = useState(false);
  const [terminating, setTerminating] = useState<SalesContactEmployment | null>(null);
  const [deletingTarget, setDeletingTarget] = useState<SalesContactEmployment | null>(null);

  const activeCount = employments.filter((e) => e.active).length;

  const handleDelete = async () => {
    if (!deletingTarget) return;
    try {
      await deleteMut({
        id: deletingTarget.id,
        contactId,
        customerId: deletingTarget.customerId,
      }).unwrap();
      snackbar.success('재직 이력이 삭제되었습니다.');
      setDeletingTarget(null);
    } catch (err) {
      snackbar.error((err as ApiError)?.message ?? '삭제 중 오류가 발생했습니다.');
    }
  };

  return (
    <SectionRoot>
      <SectionHeader>
        <SectionTitle>
          <WorkRoundedIcon sx={{ fontSize: 18, color: 'primary.main' }} />
          재직 이력
          <SectionTitleCount>(현재 {activeCount} / 전체 {employments.length})</SectionTitleCount>
        </SectionTitle>
        {canWrite && (
          <SecondaryPageHeaderButton
            startIcon={<AddRoundedIcon sx={{ fontSize: 18 }} />}
            onClick={() => setCreating(true)}
          >
            재직 등록
          </SecondaryPageHeaderButton>
        )}
      </SectionHeader>

      {employments.length === 0 ? (
        <EmptySection>등록된 재직 이력이 없습니다.</EmptySection>
      ) : (
        <ItemList>
          {employments.map((e) => (
            <ItemCard key={e.id} sx={{ opacity: e.active ? 1 : 0.7 }}>
              <ItemHeader>
                <ItemHeaderLeft>
                  <EmploymentStatusIndicator active={e.active} endDate={e.endDate} departureType={e.departureType} />
                  <ItemTitle>{e.customerName ?? e.externalCompanyName ?? <Muted />}</ItemTitle>
                  <ItemSubtle>
                    {[e.position, e.department].filter(Boolean).join(' · ')}
                  </ItemSubtle>
                </ItemHeaderLeft>
                {canWrite && (
                  <ItemHeaderRight>
                    {e.active && (
                      <>
                        <Tooltip title="수정" arrow>
                          <IconButton size="small" onClick={() => setEditing(e)}>
                            <EditOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="재직 종료" arrow>
                          <IconButton
                            size="small"
                            onClick={() => setTerminating(e)}
                            sx={{ '&:hover': { color: 'warning.main' } }}
                          >
                            <StopCircleOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </>
                    )}
                    <Tooltip title="삭제" arrow>
                      <IconButton
                        size="small"
                        onClick={() => setDeletingTarget(e)}
                        sx={{ '&:hover': { color: 'error.main' } }}
                      >
                        <DeleteOutlineIcon fontSize="small" />
                      </IconButton>
                    </Tooltip>
                  </ItemHeaderRight>
                )}
              </ItemHeader>
              <ItemSubtle>
                기간: {e.startDate} ~ {e.endDate ?? '현재'}
                {e.departureNote ? ` · ${e.departureNote}` : ''}
              </ItemSubtle>
            </ItemCard>
          ))}
        </ItemList>
      )}

      <EmploymentFormModal open={creating} onClose={() => setCreating(false)} contactId={contactId} />
      <EmploymentFormModal
        open={editing !== null}
        onClose={() => setEditing(null)}
        contactId={contactId}
        employment={editing ?? undefined}
      />
      {terminating && (
        <EmploymentTerminateModal
          open={terminating !== null}
          onClose={() => setTerminating(null)}
          contactId={contactId}
          employment={terminating}
        />
      )}
      <ConfirmModal
        isOpen={deletingTarget !== null}
        title="재직 이력 삭제"
        message={`${deletingTarget?.customerName ?? deletingTarget?.externalCompanyName ?? '회사'} 재직 이력을 삭제하시겠습니까?`}
        confirmLabel={isDeleting ? '삭제 중...' : '삭제'}
        danger
        onConfirm={handleDelete}
        onCancel={() => setDeletingTarget(null)}
      />
    </SectionRoot>
  );
}
