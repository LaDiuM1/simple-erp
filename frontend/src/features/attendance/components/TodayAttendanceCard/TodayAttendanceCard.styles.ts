import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';

export const CardRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 12,
  padding: '1rem 1.375rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
  [theme.breakpoints.down('sm')]: { padding: '0.875rem 1rem 1rem' },
}));

export const CardHeaderRow = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.5rem',
});

export const CardTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.005em',
}));

export const CardDate = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  fontVariantNumeric: 'tabular-nums',
}));

export const StatGrid = styled(Box)({
  display: 'grid',
  gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
  gap: '1rem',
});

export const StatBlock = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.25rem',
  minWidth: 0,
});

export const StatLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  fontWeight: 500,
  letterSpacing: '0.02em',
}));

export const StatValue = styled(Typography)(({ theme }) => ({
  fontSize: '1.375rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
  letterSpacing: '-0.025em',
  lineHeight: 1.2,
  fontVariantNumeric: 'tabular-nums',
}));

/** 반경 내 / 밖 텍스트 자리 — 색은 WithinRangeText 가 결정. */
export const StatSub = styled(Box)({
  fontSize: '0.75rem',
});

export const ButtonRow = styled(Box)({
  display: 'flex',
  gap: '0.5rem',
});

const actionButtonBase = {
  height: 36,
  paddingLeft: '1rem',
  paddingRight: '1rem',
  fontSize: '0.8125rem',
  fontWeight: 600,
  borderRadius: 0,
  textTransform: 'none' as const,
  letterSpacing: '-0.005em',
  boxShadow: 'none',
};

export const CheckInButton = styled(Button)(({ theme }) => ({
  ...actionButtonBase,
  backgroundColor: theme.palette.primary.main,
  color: theme.palette.primary.contrastText,
  border: `1px solid ${theme.palette.primary.main}`,
  '&:hover': {
    backgroundColor: theme.palette.primary.dark,
    borderColor: theme.palette.primary.dark,
  },
  '&.Mui-disabled': {
    backgroundColor: theme.palette.primary.main,
    borderColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    opacity: 0.5,
  },
}));

export const CheckOutButton = styled(Button)(({ theme }) => ({
  ...actionButtonBase,
  fontWeight: 500,
  backgroundColor: 'transparent',
  color: theme.palette.text.secondary,
  border: `1px solid ${theme.palette.divider}`,
  '&:hover': {
    backgroundColor: theme.palette.background.default,
    borderColor: theme.palette.text.disabled,
    color: theme.palette.text.primary,
  },
  '&.Mui-disabled': {
    color: theme.palette.text.disabled,
    borderColor: theme.palette.divider,
  },
}));
