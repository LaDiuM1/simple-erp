import { useState } from 'react';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import PeopleAltRoundedIcon from '@mui/icons-material/PeopleAltRounded';
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
import { useDeleteSalesAssignmentMutation } from '@/features/salesCustomer/api/salesCustomerApi';
import type { SalesAssignment } from '@/features/salesCustomer/types';
import { getErrorMessage } from '@/shared/api/error';
import AssignmentStatusIndicator from './AssignmentStatusIndicator';
import AssignmentFormModal from './AssignmentFormModal/AssignmentFormModal';
import AssignmentTerminateModal from './AssignmentTerminateModal/AssignmentTerminateModal';
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
} from './salesCustomerDetail.styles';

interface Props {
  customerId: number;
  assignments: SalesAssignment[];
}

export default function AssignmentList({ customerId, assignments }: Props) {
  const { canWrite } = usePermission(MENU_CODE.SALES_CUSTOMERS);
  const snackbar = useSnackbar();
  const [deleteMut, { isLoading: isDeleting }] = useDeleteSalesAssignmentMutation();

  const [editing, setEditing] = useState<SalesAssignment | null>(null);
  const [creating, setCreating] = useState(false);
  const [terminating, setTerminating] = useState<SalesAssignment | null>(null);
  const [deletingTarget, setDeletingTarget] = useState<SalesAssignment | null>(null);

  const activeCount = assignments.filter((a) => a.active).length;

  const handleDelete = async () => {
    if (!deletingTarget) return;
    try {
      await deleteMut({ id: deletingTarget.id, customerId }).unwrap();
      snackbar.success('배정이 삭제되었습니다.');
      setDeletingTarget(null);
    } catch (err) {
      snackbar.error(getErrorMessage(err, '삭제 중 오류가 발생했습니다.'));
    }
  };

  return (
    <SectionRoot>
      <SectionHeader>
        <SectionTitle>
          <PeopleAltRoundedIcon sx={{ fontSize: 18, color: 'primary.main' }} />
          영업 담당자
          <SectionTitleCount>
            (활성 {activeCount} / 전체 {assignments.length})
          </SectionTitleCount>
        </SectionTitle>
        {canWrite && (
          <SecondaryPageHeaderButton
            startIcon={<AddRoundedIcon sx={{ fontSize: 18 }} />}
            onClick={() => setCreating(true)}
          >
            담당자 배정
          </SecondaryPageHeaderButton>
        )}
      </SectionHeader>

      {assignments.length === 0 ? (
        <EmptySection>배정된 영업 담당자가 없습니다.</EmptySection>
      ) : (
        <ItemList>
          {assignments.map((a) => (
            <ItemCard key={a.id} sx={{ opacity: a.active ? 1 : 0.7 }}>
              <ItemHeader>
                <ItemHeaderLeft>
                  <AssignmentStatusIndicator
                    active={a.active}
                    primary={a.primary}
                    endDate={a.endDate}
                  />
                  <ItemTitle>{a.employeeName ?? <Muted />}</ItemTitle>
                  <ItemSubtle>
                    {[a.employeeDepartmentName, a.employeePositionName]
                      .filter(Boolean)
                      .join(' · ')}
                  </ItemSubtle>
                </ItemHeaderLeft>
                {canWrite && (
                  <ItemHeaderRight>
                    {a.active && (
                      <>
                        <Tooltip title="수정" arrow>
                          <IconButton size="small" onClick={() => setEditing(a)}>
                            <EditOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="배정 종료" arrow>
                          <IconButton
                            size="small"
                            onClick={() => setTerminating(a)}
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
                기간: {a.startDate} ~ {a.endDate ?? '현재'}
                {a.reason ? ` · 사유: ${a.reason}` : ''}
              </ItemSubtle>
            </ItemCard>
          ))}
        </ItemList>
      )}

      <AssignmentFormModal
        open={creating}
        onClose={() => setCreating(false)}
        customerId={customerId}
      />
      <AssignmentFormModal
        open={editing !== null}
        onClose={() => setEditing(null)}
        customerId={customerId}
        assignment={editing ?? undefined}
      />
      {terminating && (
        <AssignmentTerminateModal
          open={terminating !== null}
          onClose={() => setTerminating(null)}
          customerId={customerId}
          assignment={terminating}
        />
      )}
      <ConfirmModal
        isOpen={deletingTarget !== null}
        title="배정 삭제"
        message={`${deletingTarget?.employeeName ?? '담당자'} 의 배정 이력을 삭제하시겠습니까? (이력에서 완전히 제거)`}
        confirmLabel={isDeleting ? '삭제 중...' : '삭제'}
        danger
        onConfirm={handleDelete}
        onCancel={() => setDeletingTarget(null)}
      />
    </SectionRoot>
  );
}
