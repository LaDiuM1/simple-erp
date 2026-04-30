import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import StopCircleOutlinedIcon from '@mui/icons-material/StopCircleOutlined';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import {
  InlineLinkButton,
  NowrapText,
  RowActions,
  StatusText,
  TabPrimaryActionButton,
  tabbedTab,
  type TabbedTab,
  type TabbedTableColumn,
  type TabHookResult,
} from '@/shared/ui/GenericTabbedTable';
import { usePermission } from '@/shared/hooks/usePermission';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import Muted from '@/shared/ui/atoms/Muted';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useDeleteSalesContactEmploymentMutation } from '@/features/salesContact/api/salesContactApi';
import {
  DEPARTURE_TYPE_LABELS,
  type SalesContactEmployment,
} from '@/features/salesContact/types';
import EmploymentFormModal from '@/features/salesContact/components/EmploymentFormModal/EmploymentFormModal';
import EmploymentTerminateModal from '@/features/salesContact/components/EmploymentTerminateModal/EmploymentTerminateModal';
import { getErrorMessage } from '@/shared/api/error';

/**
 * 재직 이력 탭 — 등록 / 수정 / 종료 / 삭제 모달 ownership.
 * 비활성 행은 opacity 로 dim, 상태 텍스트는 활성=primary / 비활성=disabled 색.
 */
export function useEmploymentTab(
  contactId: number,
  employments: SalesContactEmployment[],
): TabHookResult {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.SALES_CONTACTS);
  const snackbar = useSnackbar();
  const [deleteMut, { isLoading: isDeleting }] = useDeleteSalesContactEmploymentMutation();

  const [editing, setEditing] = useState<SalesContactEmployment | null>(null);
  const [creating, setCreating] = useState(false);
  const [terminating, setTerminating] = useState<SalesContactEmployment | null>(null);
  const [deletingTarget, setDeletingTarget] = useState<SalesContactEmployment | null>(null);

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
      snackbar.error(getErrorMessage(err, '삭제 중 오류가 발생했습니다.'));
    }
  };

  const columns: TabbedTableColumn<SalesContactEmployment>[] = [
    {
      key: 'status',
      header: '상태',
      width: 88,
      render: (e) =>
        e.active ? (
          <StatusText tone="primary">재직 중</StatusText>
        ) : (
          <StatusText tone="disabled">
            {e.departureType ? DEPARTURE_TYPE_LABELS[e.departureType] : '종료'}
          </StatusText>
        ),
    },
    {
      key: 'company',
      header: '회사',
      render: (e) => {
        if (e.customerId) {
          return (
            <InlineLinkButton
              type="button"
              onClick={(ev) => {
                ev.stopPropagation();
                navigate(`${MENU_PATH[MENU_CODE.SALES_CUSTOMERS]}/${e.customerId}`);
              }}
              sx={{ textDecoration: 'underline', fontWeight: 500 }}
            >
              {e.customerName}
            </InlineLinkButton>
          );
        }
        return e.externalCompanyName ?? <Muted />;
      },
    },
    {
      key: 'position',
      header: '직책',
      width: 132,
      render: (e) => e.position ?? <Muted />,
    },
    {
      key: 'department',
      header: '부서',
      width: 144,
      render: (e) => e.department ?? <Muted />,
    },
    {
      key: 'startDate',
      header: '시작일',
      width: 108,
      render: (e) => <NowrapText>{e.startDate}</NowrapText>,
    },
    {
      key: 'endDate',
      header: '종료일',
      width: 108,
      render: (e) => (e.endDate ? <NowrapText>{e.endDate}</NowrapText> : <Muted />),
    },
    {
      key: 'departureNote',
      header: '퇴직 메모',
      render: (e) => e.departureNote ?? <Muted />,
    },
  ];

  if (canWrite) {
    columns.push({
      key: 'actions',
      header: '액션',
      align: 'right',
      width: 104,
      render: (e) => (
        <RowActions>
          {e.active && (
            <>
              <Tooltip title="수정" arrow>
                <IconButton
                  size="small"
                  onClick={(ev) => {
                    ev.stopPropagation();
                    setEditing(e);
                  }}
                >
                  <EditOutlinedIcon fontSize="small" />
                </IconButton>
              </Tooltip>
              <Tooltip title="재직 종료" arrow>
                <IconButton
                  size="small"
                  onClick={(ev) => {
                    ev.stopPropagation();
                    setTerminating(e);
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
                setDeletingTarget(e);
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

  const tab: TabbedTab<SalesContactEmployment> = {
    key: 'employments',
    label: '재직 이력',
    count: employments.length,
    rows: employments,
    rowKey: (e) => e.id,
    rowSx: (e) => ({ opacity: e.active ? 1 : 0.7 }),
    columns,
    emptyMessage: '등록된 재직 이력이 없습니다.',
    rightSlot: canWrite ? (
      <TabPrimaryActionButton
        startIcon={<AddRoundedIcon />}
        onClick={() => setCreating(true)}
      >
        재직 등록
      </TabPrimaryActionButton>
    ) : null,
  };

  const modals = (
    <>
      <EmploymentFormModal
        open={creating}
        onClose={() => setCreating(false)}
        contactId={contactId}
      />
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
    </>
  );

  return { tab: tabbedTab(tab), modals };
}
