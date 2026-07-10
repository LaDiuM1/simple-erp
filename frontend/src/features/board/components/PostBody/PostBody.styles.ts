import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

/**
 * 본문 박스 — 상단 보더는 의도적으로 없음 (GenericHeaderDetails 의 bottom border 가
 * 시각적 상단선 역할). 하단 보더가 댓글 섹션과의 구분선을 겸한다.
 */
export const BodyRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  borderLeft: `1px solid ${theme.palette.divider}`,
  borderRight: `1px solid ${theme.palette.divider}`,
  borderBottom: `1px solid ${theme.palette.divider}`,
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '1.25rem',
  [theme.breakpoints.up('md')]: {
    padding: '2rem',
  },
}));

export const ContentText = styled(Typography)(({ theme }) => ({
  fontSize: '0.875rem',
  color: theme.palette.text.primary,
  lineHeight: 1.7,
  whiteSpace: 'pre-wrap',
  wordBreak: 'break-word',
}));

export const AttachmentList = styled(Box)(({ theme }) => ({
  borderTop: `1px dashed ${theme.palette.divider}`,
  paddingTop: '1rem',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'flex-start',
  gap: '0.375rem',
}));

/** 첨부 파일명 링크 — 클릭 시 다운로드 (button 시맨틱 + 링크 톤). */
export const AttachmentLink = styled('button')(({ theme }) => ({
  background: 'none',
  border: 'none',
  padding: 0,
  margin: 0,
  cursor: 'pointer',
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.25rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.primary.main,
  textAlign: 'left',
  '&:hover': {
    textDecoration: 'underline',
  },
}));
