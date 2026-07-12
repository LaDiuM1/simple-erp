import * as React from 'react';
import { useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import TextField from '@mui/material/TextField';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import GenericDetailModal from '@/shared/ui/GenericDetailModal';
import { formatDateTime } from '@/shared/ui/GenericTabbedTable';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  useCreateContractNoteMutation,
  useDeleteContractNoteMutation,
} from '@/features/contract/api/contractApi';
import type { NoteTabModalProps } from '@/features/contract/hooks/useNoteTab';

interface Props {
  modal: NoteTabModalProps;
}

/** 변경 이력 탭의 모달 묶음 — 메모 등록 폼 + 풀 컨텐츠 detail + 삭제 확인. */
export default function NoteTabModals({ modal }: Props) {
  const submit = useApiSubmit();
  const [deleteNote, { isLoading: isDeleting }] = useDeleteContractNoteMutation();

  const handleDelete = async () => {
    if (!modal.deleting) return;
    await submit(deleteNote({ id: modal.deleting.id, contractId: modal.contractId }), {
      success: '메모가 삭제되었습니다.',
      onSuccess: modal.onCloseDelete,
    });
  };

  return (
    <>
      <NoteCreateModal
        open={modal.creating}
        onClose={modal.onCloseCreate}
        contractId={modal.contractId}
      />
      <GenericDetailModal
        open={modal.viewing !== null}
        onClose={modal.onCloseView}
        title="변경 이력 메모"
        fields={
          modal.viewing
            ? [
                { label: '일시', value: formatDateTime(modal.viewing.createdAt) },
                { label: '작성자', value: modal.viewing.authorName },
                { label: '내용', value: modal.viewing.content },
              ]
            : []
        }
      />
      <ConfirmModal
        isOpen={modal.deleting !== null}
        title="메모 삭제"
        message="선택한 변경 이력 메모를 삭제하시겠습니까?"
        confirmLabel={isDeleting ? '삭제 중...' : '삭제'}
        danger
        confirmDisabled={isDeleting}
        onConfirm={handleDelete}
        onCancel={modal.onCloseDelete}
      />
    </>
  );
}

function NoteCreateModal({
  open,
  onClose,
  contractId,
}: {
  open: boolean;
  onClose: () => void;
  contractId: number;
}) {
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const [createNote, { isLoading: isSaving }] = useCreateContractNoteMutation();
  const [content, setContent] = useState('');

  React.useEffect(() => {
    if (open) setContent('');
  }, [open]);

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    if (content.trim() === '') {
      snackbar.error('메모 내용을 입력해주세요.');
      return;
    }
    await submit(createNote({ contractId, content: content.trim() }), {
      success: '메모가 등록되었습니다.',
      onSuccess: onClose,
    });
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>변경 이력 메모 등록</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <TextField
            fullWidth
            size="small"
            label="내용"
            required
            multiline
            minRows={3}
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="계약일 변경 / 설비 변경 등 변경 내용과 사유"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={isSaving}>취소</Button>
          <Button type="submit" variant="contained" disabled={isSaving}>
            {isSaving ? '등록 중...' : '등록'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
