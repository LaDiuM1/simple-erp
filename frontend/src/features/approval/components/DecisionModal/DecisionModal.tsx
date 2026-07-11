import { useState } from 'react';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import {
  CommentField,
  DecisionDialog,
  ModalCancelButton,
  ModalConfirmButton,
} from './DecisionModal.styles';

export type DecisionMode = 'approve' | 'reject';

interface Props {
  open: boolean;
  mode: DecisionMode;
  isSaving: boolean;
  onClose: () => void;
  onSubmit: (comment: string) => void;
}

/**
 * 승인 / 반려 의견 입력 모달 — ConfirmModal 톤의 소형 다이얼로그 + 의견 textarea 1개.
 * 의견은 선택 입력 — 빈 값 제출 허용 (BE DecisionRequest.comment 선택).
 */
export default function DecisionModal({ open, mode, isSaving, onClose, onSubmit }: Props) {
  const [comment, setComment] = useState('');
  const isApprove = mode === 'approve';

  return (
    <DecisionDialog open={open} onClose={onClose}>
      <DialogTitle sx={{ p: 0, fontSize: '1rem', fontWeight: 600 }}>
        {isApprove ? '승인' : '반려'}
      </DialogTitle>
      <DialogContent sx={{ p: 0, overflow: 'visible' }}>
        <CommentField
          size="small"
          fullWidth
          multiline
          minRows={3}
          autoFocus
          value={comment}
          onChange={(e) => setComment(e.target.value)}
          placeholder="의견을 입력하세요 (선택)"
        />
      </DialogContent>
      <DialogActions sx={{ p: 0, mt: '0.5rem', gap: '0.5rem' }}>
        <ModalCancelButton variant="outlined" onClick={onClose} disabled={isSaving}>
          취소
        </ModalCancelButton>
        <ModalConfirmButton
          variant="contained"
          color={isApprove ? 'primary' : 'error'}
          onClick={() => onSubmit(comment)}
          disabled={isSaving}
        >
          {isSaving ? '처리 중...' : isApprove ? '승인' : '반려'}
        </ModalConfirmButton>
      </DialogActions>
    </DecisionDialog>
  );
}
