import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import AssignmentFormModal from '@/features/salesCustomer/components/AssignmentFormModal/AssignmentFormModal';
import AssignmentTerminateModal from '@/features/salesCustomer/components/AssignmentTerminateModal/AssignmentTerminateModal';
import type { SalesAssignment } from '@/features/salesCustomer/types';

export interface AssignmentTabModalProps {
  customerId: number;
  creating: boolean;
  editing: SalesAssignment | null;
  terminating: SalesAssignment | null;
  deletingTarget: SalesAssignment | null;
  isDeleting: boolean;
  onCloseCreate: () => void;
  onCloseEdit: () => void;
  onCloseTerminate: () => void;
  onCancelDelete: () => void;
  onConfirmDelete: () => void;
}

export default function AssignmentTabModals({ modal }: { modal: AssignmentTabModalProps }) {
  return (
    <>
      <AssignmentFormModal
        open={modal.creating}
        onClose={modal.onCloseCreate}
        customerId={modal.customerId}
      />
      <AssignmentFormModal
        open={modal.editing !== null}
        onClose={modal.onCloseEdit}
        customerId={modal.customerId}
        assignment={modal.editing ?? undefined}
      />
      {modal.terminating && (
        <AssignmentTerminateModal
          open={modal.terminating !== null}
          onClose={modal.onCloseTerminate}
          customerId={modal.customerId}
          assignment={modal.terminating}
        />
      )}
      <ConfirmModal
        isOpen={modal.deletingTarget !== null}
        title="배정 삭제"
        message={`${modal.deletingTarget?.employeeName ?? '담당자'} 의 배정 이력을 삭제하시겠습니까? (이력에서 완전히 제거)`}
        confirmLabel={modal.isDeleting ? '삭제 중...' : '삭제'}
        danger
        onConfirm={modal.onConfirmDelete}
        onCancel={modal.onCancelDelete}
      />
    </>
  );
}
