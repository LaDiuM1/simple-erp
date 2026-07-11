import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

export const CreateRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  [theme.breakpoints.up('sm')]: {
    margin: '-2rem',
  },
}));

export const CreateForm = styled('form')(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  [theme.breakpoints.up('md')]: {
    padding: '2rem',
  },
}));

/** 세로 1-column 필드 배열 — 기안 폼은 제목 / 본문 / 결재선 / 첨부 모두 전체 폭. */
export const FieldStack = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
});
