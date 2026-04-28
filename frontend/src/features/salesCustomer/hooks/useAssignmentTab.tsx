import { useState } from 'react';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import StopCircleOutlinedIcon from '@mui/icons-material/StopCircleOutlined';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import {
  NowrapText,
  RowActions,
  StatusText,
  TabPrimaryActionButton,
  tabbedTab,
  type TabbedTab,
  type TabbedTableColumn,
  type TabHookResult,
} from '@/shared/ui/GenericTabbedTable';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import Muted from '@/shared/ui/atoms/Muted';
import { usePermission } from '@/shared/hooks/usePermission';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { getErrorMessage } from '@/shared/api/error';
import { useDeleteSalesAssignmentMutation } from '@/features/salesCustomer/api/salesCustomerApi';
import type { SalesAssignment } from '@/features/salesCustomer/types';
import AssignmentFormModal from '@/features/salesCustomer/components/AssignmentFormModal/AssignmentFormModal';
import AssignmentTerminateModal from '@/features/salesCustomer/components/AssignmentTerminateModal/AssignmentTerminateModal';

/**
 * 영업 고객사 상세의 담당자 탭 — 등록/수정/종료/삭제 모달 owner.
 * 표시 정렬: 주담당 → 활성 → startDate desc (BE 는 endDate/startDate 만 정렬하므로 표시층에서 보정).
 * 비활성 행은 opacity dim, 상태 텍스트는 주담당=primary / 담당=success / 종료=disabled 색.
 */
export function useAssignmentTab(
  customerId: number,
  assignments: SalesAssignment[],
): TabHookResult {
  const { canWrite } = usePermission(MENU_CODE.SALES_CUSTOMERS);
  const snackbar = useSnackbar();
  const [deleteMut, { isLoading: isDeleting }] = useDeleteSalesAssignmentMutation();

  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<SalesAssignment | null>(null);
  const [terminating, setTerminating] = useState<SalesAssignment | null>(null);
  const [deletingTarget, setDeletingTarget] = useState<SalesAssignment | null>(null);

  const sorted = [...assignments].sort((a, b) => {
    if (a.primary !== b.primary) return a.primary ? -1 : 1;
    if (a.active !== b.active) return a.active ? -1 : 1;
    return b.startDate.localeCompare(a.startDate);
  });

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

  const columns: TabbedTableColumn<SalesAssignment>[] = [
    {
      key: 'status',
      header: '상태',
      width: 88,
      render: (a) => {
        if (!a.active) return <StatusText tone="disabled">종료</StatusText>;
        return (
          <StatusText tone={a.primary ? 'primary' : 'success'}>
            {a.primary ? '주담당' : '담당'}
          </StatusText>
        );
      },
    },
    {
      key: 'employee',
      header: '담당자',
      render: (a) => a.employeeName ?? <Muted />,
    },
    {
      key: 'department',
      header: '부서',
      width: 144,
      render: (a) => a.employeeDepartmentName ?? <Muted />,
    },
    {
      key: 'position',
      header: '직책',
      width: 132,
      render: (a) => a.employeePositionName ?? <Muted />,
    },
    {
      key: 'startDate',
      header: '시작일',
      width: 108,
      render: (a) => <NowrapText>{a.startDate}</NowrapText>,
    },
    {
      key: 'endDate',
      header: '종료일',
      width: 108,
      render: (a) => (a.endDate ? <NowrapText>{a.endDate}</NowrapText> : <Muted />),
    },
    {
      key: 'reason',
      header: '사유',
      render: (a) => a.reason ?? <Muted />,
    },
  ];

  if (canWrite) {
    columns.push({
      key: 'actions',
      header: '액션',
      align: 'right',
      width: 104,
      render: (a) => (
        <RowActions>
          {a.active && (
            <>
              <Tooltip title="수정" arrow>
                <IconButton
                  size="small"
                  onClick={(ev) => {
                    ev.stopPropagation();
                    setEditing(a);
                  }}
                >
                  <EditOutlinedIcon fontSize="small" />
                </IconButton>
              </Tooltip>
              <Tooltip title="배정 종료" arrow>
                <IconButton
                  size="small"
                  onClick={(ev) => {
                    ev.stopPropagation();
                    setTerminating(a);
                  }}
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
              onClick={(ev) => {
                ev.stopPropagation();
                setDeletingTarget(a);
              }}
              sx={{ '&:hover': { color: 'error.main' } }}
            >
              <DeleteOutlineIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        </RowActions>
      ),
    });
  }

  const tab: TabbedTab<SalesAssignment> = {
    key: 'assignments',
    label: '영업 담당자',
    count: assignments.length,
    rows: sorted,
    rowKey: (a) => a.id,
    rowSx: (a) => ({ opacity: a.active ? 1 : 0.7 }),
    columns,
    emptyMessage: '배정된 영업 담당자가 없습니다.',
    rightSlot: canWrite ? (
      <TabPrimaryActionButton
        startIcon={<AddRoundedIcon />}
        onClick={() => setCreating(true)}
      >
        담당자 배정
      </TabPrimaryActionButton>
    ) : null,
  };

  const modals = (
    <>
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
    </>
  );

  return { tab: tabbedTab(tab), modals };
}
