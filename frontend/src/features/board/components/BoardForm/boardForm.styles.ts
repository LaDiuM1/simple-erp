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

/** 섹션 내부 필드 세로 스택 — 필드 간 간격 통일. */
export const FieldColumn = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
});

/** 카테고리 (고정 폭) + 제목 (나머지) — md 미만에서는 1열 스택. */
export const CategoryTitleGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '1rem',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: '200px minmax(0, 1fr)',
  },
}));
