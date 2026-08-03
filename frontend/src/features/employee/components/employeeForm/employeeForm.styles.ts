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

export const FieldGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '1rem',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: '1fr 1fr',
  },
}));

export const FieldFull = styled(Box)(({ theme }) => ({
  [theme.breakpoints.up('md')]: {
    gridColumn: '1 / -1',
  },
}));

/** md+ 에서 두 번째 컬럼에 배치 (좌측 빈 칸). 모바일에선 자연 스택. */
export const FieldCol2 = styled(Box)(({ theme }) => ({
  [theme.breakpoints.up('md')]: {
    gridColumn: '2',
  },
}));
