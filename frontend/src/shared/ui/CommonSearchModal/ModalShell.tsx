import type { ReactNode } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import { ModalContent, ModalTitleRow } from './CommonSearchModal.styles';

interface Props {
  open: boolean;
  onClose: () => void;
  title: string;
  headerActions?: ReactNode;
  /** 본문 — 필터바 / 테이블 / pagination 등을 포함. ModalContent 안에 그대로 렌더된다. */
  children: ReactNode;
  /** DialogActions 에 들어갈 버튼들. */
  footer: ReactNode;
}

/**
 * 검색 / 관리 모달의 외곽 shell — Dialog + Title + ModalContent + DialogActions.
 * `paper.maxHeight: 85vh` 로 좁은 뷰포트에서 컨텐츠가 넘치면 본문 (ModalContent) 자체가 스크롤.
 */
export default function ModalShell({ open, onClose, title, headerActions, children, footer }: Props) {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="xl"
      fullWidth
      slotProps={{ paper: { sx: { maxHeight: '85vh' } } }}
    >
      <ModalTitleRow>
        <span>{title}</span>
        {headerActions && <span>{headerActions}</span>}
      </ModalTitleRow>
      <ModalContent>{children}</ModalContent>
      <DialogActions>{footer}</DialogActions>
    </Dialog>
  );
}
