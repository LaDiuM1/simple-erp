import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import EmploymentFormModal from '@/features/salesContact/components/EmploymentFormModal/EmploymentFormModal';
import EmploymentTerminateModal from '@/features/salesContact/components/EmploymentTerminateModal/EmploymentTerminateModal';
import type { SalesContactEmployment } from '@/features/salesContact/types';

export interface EmploymentTabModalProps {
  contactId: number;
  creating: boolean;
  editing: SalesContactEmployment | null;
  terminating: SalesContactEmployment | null;
  deletingTarget: SalesContactEmployment | null;
  isDeleting: boolean;
  onCloseCreate: () => void;
  onCloseEdit: () => void;
  onCloseTerminate: () => void;
  onCancelDelete: () => void;
  onConfirmDelete: () => void;
}

export default function EmploymentTabModals({ modal }: { modal: EmploymentTabModalProps }) {
  return (
    <>
      <EmploymentFormModal
        open={modal.creating}
        onClose={modal.onCloseCreate}
        contactId={modal.contactId}
      />
      <EmploymentFormModal
        open={modal.editing !== null}
        onClose={modal.onCloseEdit}
        contactId={modal.contactId}
        employment={modal.editing ?? undefined}
      />
      {modal.terminating && (
        <EmploymentTerminateModal
          open={modal.terminating !== null}
          onClose={modal.onCloseTerminate}
          contactId={modal.contactId}
          employment={modal.terminating}
        />
      )}
      <ConfirmModal
        isOpen={modal.deletingTarget !== null}
        title="재직 이력 삭제"
        message={`${modal.deletingTarget?.customerName ?? modal.deletingTarget?.externalCompanyName ?? '회사'} 재직 이력을 삭제하시겠습니까?`}
        confirmLabel={modal.isDeleting ? '삭제 중...' : '삭제'}
        danger
        onConfirm={modal.onConfirmDelete}
        onCancel={modal.onCancelDelete}
      />
    </>
  );
}
