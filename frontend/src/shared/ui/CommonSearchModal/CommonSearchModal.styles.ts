import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import DialogContent from '@mui/material/DialogContent';

/** 모달 본문 — 필터/테이블/페이지네이션을 세로 스택. 패딩 제거 후 자체 영역별 padding 사용. */
export const ModalContent = styled(DialogContent)(({ theme }) => ({
  padding: 0,
  display: 'flex',
  flexDirection: 'column',
  minHeight: 0,
  borderTop: `1px solid ${theme.palette.divider}`,
  borderBottom: `1px solid ${theme.palette.divider}`,
}));

/** 필터바 영역 — 패딩 + 하단 구분선. */
export const ModalFilterArea = styled(Box)(({ theme }) => ({
  padding: '0.875rem 1rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  flexShrink: 0,
}));
