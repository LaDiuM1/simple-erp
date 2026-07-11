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

/** 출력 값 + 단위처럼 한 칸 안에서 좌우로 나눠 쓰는 페어 필드. */
export const FieldPair = styled(Box)({
  display: 'flex',
  gap: '0.5rem',
  '& > *:first-of-type': { flex: 1.4 },
  '& > *:last-of-type': { flex: 1 },
});
