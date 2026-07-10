import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import DecisionModal, { type DecisionMode } from '@/features/approval/components/DecisionModal/DecisionModal';

export interface ApprovalDetailModalProps {
  decision: DecisionMode | null;
  isDeciding: boolean;
  onCloseDecision: () => void;
  onSubmitDecision: (comment: string) => void;
  cancelOpen: boolean;
  isCanceling: boolean;
  onConfirmCancel: () => void;
  onCloseCancel: () => void;
}

export default function ApprovalDetailModals({ modal }: { modal: ApprovalDetailModalProps }) {
  return (
    <>
      {modal.decision && (
        <DecisionModal
          open
          mode={modal.decision}
          isSaving={modal.isDeciding}
          onClose={modal.onCloseDecision}
          onSubmit={modal.onSubmitDecision}
        />
      )}
      <ConfirmModal
        isOpen={modal.cancelOpen}
        title="상신 취소"
        message="이 문서의 상신을 취소하시겠습니까?"
        confirmLabel={modal.isCanceling ? '취소 중...' : '상신 취소'}
        confirmDisabled={modal.isCanceling}
        danger
        onConfirm={modal.onConfirmCancel}
        onCancel={modal.onCloseCancel}
      />
    </>
  );
}
