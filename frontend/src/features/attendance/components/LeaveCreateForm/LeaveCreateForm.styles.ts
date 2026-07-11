import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

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

/** 예상 차감 일수 요약 패널 — 기간 입력 아래 실시간 갱신. */
export const DeductionSummary = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.headerBg,
  border: `1px solid ${theme.palette.divider}`,
  padding: '0.75rem 1rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.25rem',
}));

export const DeductionRow = styled(Box)({
  display: 'flex',
  alignItems: 'baseline',
  gap: '0.5rem',
});

export const DeductionLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
}));

export const DeductionValue = styled('span', {
  shouldForwardProp: (prop) => prop !== 'insufficient',
})<{ insufficient?: boolean }>(({ theme, insufficient }) => ({
  fontSize: '1rem',
  fontWeight: 700,
  fontVariantNumeric: 'tabular-nums',
  color: insufficient ? theme.palette.error.main : theme.palette.primary.main,
}));

export const DeductionHint = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.disabled,
}));

export const DeductionWarning = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  fontWeight: 500,
  color: theme.palette.error.main,
}));
