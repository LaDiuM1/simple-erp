import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import GenericDetailModal, { type DetailModalField } from '@/shared/ui/GenericDetailModal';
import ActivityFormModal from '@/features/salesCustomer/components/ActivityFormModal/ActivityFormModal';
import type { SalesActivity } from '@/features/salesCustomer/types';

export interface CustomerActivityTabModalProps {
  customerId: number;
  creating: boolean;
  editing: SalesActivity | null;
  deletingTarget: SalesActivity | null;
  isDeleting: boolean;
  detailTarget: SalesActivity | null;
  detailFields: DetailModalField[];
  onCloseCreate: () => void;
  onCloseEdit: () => void;
  onCancelDelete: () => void;
  onConfirmDelete: () => void;
  onCloseDetail: () => void;
}

export default function CustomerActivityTabModals({
  modal,
}: {
  modal: CustomerActivityTabModalProps;
}) {
  return (
    <>
      <ActivityFormModal
        open={modal.creating}
        onClose={modal.onCloseCreate}
        customerId={modal.customerId}
      />
      <ActivityFormModal
        open={modal.editing !== null}
        onClose={modal.onCloseEdit}
        customerId={modal.customerId}
        activity={modal.editing ?? undefined}
      />
      <ConfirmModal
        isOpen={modal.deletingTarget !== null}
        title="영업 활동 삭제"
        message={`"${modal.deletingTarget?.subject ?? ''}" 활동을 삭제하시겠습니까?`}
        confirmLabel={modal.isDeleting ? '삭제 중...' : '삭제'}
        danger
        onConfirm={modal.onConfirmDelete}
        onCancel={modal.onCancelDelete}
      />
      <GenericDetailModal
        open={modal.detailTarget !== null}
        onClose={modal.onCloseDetail}
        title={modal.detailTarget?.subject ?? '영업 활동'}
        fields={modal.detailFields}
      />
    </>
  );
}
