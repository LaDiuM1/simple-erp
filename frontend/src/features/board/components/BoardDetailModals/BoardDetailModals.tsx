import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';

export interface BoardDetailModalProps {
  deleteOpen: boolean;
  isDeleting: boolean;
  deleteMessage: string;
  onConfirmDelete: () => void;
  onCloseDelete: () => void;
}

/** 게시글 상세 모달 묶음 — 삭제 확인 (ApprovalDetailModals 와 동형). */
export default function BoardDetailModals({ modal }: { modal: BoardDetailModalProps }) {
  return (
    <ConfirmModal
      isOpen={modal.deleteOpen}
      title="게시글 삭제"
      message={modal.deleteMessage}
      confirmLabel={modal.isDeleting ? '삭제 중...' : '삭제'}
      confirmDisabled={modal.isDeleting}
      danger
      onConfirm={modal.onConfirmDelete}
      onCancel={modal.onCloseDelete}
    />
  );
}
