import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

/** 폼 페이지의 full-bleed 외곽 — 목록과 동일한 margin 탈출 + 스크롤 영역. */
export const FormRoot = styled(Box)(({ theme }) => ({
  minHeight: 0,
  margin: '-1rem',
  [theme.breakpoints.up('sm')]: {
    margin: '-2rem',
  },
  [theme.breakpoints.up('md')]: {
    height: 'calc(100% + 4rem)',
    display: 'flex',
    flexDirection: 'column',
  },
}));

/** 폼 본문 surface. 내부 스크롤 + 패딩. */
export const FormSurface = styled('form')(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  overflow: 'auto',
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
  minHeight: 0,
  [theme.breakpoints.up('md')]: {
    flex: 1,
    padding: '1.75rem 2rem',
  },
}));

/** 2열 그리드 (md+). 모바일 1열. FormField 가 span 결정. */
export const FormGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '0.75rem',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: '1fr 1fr',
  },
}));

