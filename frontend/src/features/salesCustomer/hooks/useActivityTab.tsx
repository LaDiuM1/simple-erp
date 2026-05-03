import { useState } from 'react';
import AddRoundedIcon from '@mui/icons-material/AddRounded';
import {
  TabPrimaryActionButton,
  tabbedTab,
  type AnyTabbedTab,
  type TabbedTab,
  type TabbedTableColumn,
} from '@/shared/ui/GenericTabbedTable';
import { type DetailModalField } from '@/shared/ui/GenericDetailModal';
import Muted from '@/shared/ui/atoms/Muted';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { usePermission } from '@/shared/hooks/usePermission';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { useDeleteSalesActivityMutation } from '@/features/salesCustomer/api/salesCustomerApi';
import type { SalesActivity } from '@/features/salesCustomer/types';
import type { CustomerActivityTabModalProps } from '@/features/salesCustomer/components/CustomerActivityTabModals/CustomerActivityTabModals';
import {
  activityActionsColumn,
  activityCommonColumns,
  activityHeadDetailFields,
  activityOurEmployeeColumns,
  activityOurEmployeeDetailFields,
} from './salesActivityColumns';

/**
 * 영업 고객사 상세의 활동 탭 — CRUD 모달 owner.
 * 행 클릭 시 GenericDetailModal 로 풀 컨텐츠 노출 (truncate 셀 펼치기).
 * 액션 버튼은 stopPropagation 으로 행 클릭과 분리.
 */
export function useActivityTab(
  customerId: number,
  activities: SalesActivity[],
): { tab: AnyTabbedTab; modal: CustomerActivityTabModalProps } {
  const { canWrite } = usePermission(MENU_CODE.SALES_CUSTOMERS);
  const submit = useApiSubmit();
  const [deleteMut, { isLoading: isDeleting }] = useDeleteSalesActivityMutation();

  const [creating, setCreating] = useState(false);
  const [editing, setEditing] = useState<SalesActivity | null>(null);
  const [deletingTarget, setDeletingTarget] = useState<SalesActivity | null>(null);
  const [detailTarget, setDetailTarget] = useState<SalesActivity | null>(null);

  const handleConfirmDelete = async () => {
    if (!deletingTarget) return;
    await submit(
      deleteMut({
        id: deletingTarget.id,
        customerId,
        customerContactId: deletingTarget.customerContactId,
      }),
      {
        success: '활동이 삭제되었습니다.',
        error: '삭제 중 오류가 발생했습니다.',
        onSuccess: () => setDeletingTarget(null),
      },
    );
  };

  const customerContactColumns: TabbedTableColumn<SalesActivity>[] = [
    {
      key: 'contactName',
      header: '고객 담당',
      width: 132,
      render: (a) => a.customerContactRegisteredName ?? <Muted />,
    },
    {
      key: 'contactPosition',
      header: '고객 직책',
      width: 132,
      render: (a) => a.customerContactPosition ?? <Muted />,
    },
  ];

  const columns: TabbedTableColumn<SalesActivity>[] = [
    ...activityCommonColumns,
    ...activityOurEmployeeColumns,
    ...customerContactColumns,
    ...(canWrite
      ? [activityActionsColumn({ onEdit: setEditing, onDelete: setDeletingTarget })]
      : []),
  ];

  const tab: TabbedTab<SalesActivity> = {
    key: 'activities',
    label: '영업 활동',
    count: activities.length,
    rows: activities,
    rowKey: (a) => a.id,
    columns,
    emptyMessage: '등록된 영업 활동이 없습니다.',
    onRowClick: (a) => setDetailTarget(a),
    rightSlot: canWrite ? (
      <TabPrimaryActionButton
        startIcon={<AddRoundedIcon />}
        onClick={() => setCreating(true)}
      >
        활동 등록
      </TabPrimaryActionButton>
    ) : null,
  };

  const detailFields: DetailModalField[] = detailTarget
    ? [
        ...activityHeadDetailFields(detailTarget),
        ...activityOurEmployeeDetailFields(detailTarget),
        {
          label: '고객 담당',
          value: detailTarget.customerContactRegisteredName,
        },
        { label: '고객 직책', value: detailTarget.customerContactPosition },
      ]
    : [];

  const modal: CustomerActivityTabModalProps = {
    customerId,
    creating,
    editing,
    deletingTarget,
    isDeleting,
    detailTarget,
    detailFields,
    onCloseCreate: () => setCreating(false),
    onCloseEdit: () => setEditing(null),
    onCancelDelete: () => setDeletingTarget(null),
    onConfirmDelete: handleConfirmDelete,
    onCloseDetail: () => setDetailTarget(null),
  };

  return { tab: tabbedTab(tab), modal };
}
