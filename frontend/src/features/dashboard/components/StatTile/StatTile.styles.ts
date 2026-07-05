import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const StatRoot = styled('button')(({ theme }) => ({
  appearance: 'none',
  cursor: 'pointer',
  fontFamily: 'inherit',
  textAlign: 'left',
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'center',
  gap: '0.1875rem',
  padding: '0.875rem 1.125rem',
  borderRadius: 10,
  border: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.paper,
  minWidth: 0,
  flex: 1,
  transition: 'border-color 0.15s ease',
  '&:hover': {
    borderColor: theme.palette.text.disabled,
  },
}));

export const StatLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.71875rem',
  fontWeight: 600,
  color: theme.palette.text.secondary,
}));

export const StatValueRow = styled(Box)({
  display: 'flex',
  alignItems: 'baseline',
  gap: '0.25rem',
});

export const StatValue = styled(Typography)(({ theme }) => ({
  fontSize: '1.3125rem',
  fontWeight: 760,
  color: theme.palette.text.primary,
  letterSpacing: '-0.03em',
  lineHeight: 1.05,
  fontVariantNumeric: 'tabular-nums',
}));

export const StatUnit = styled(Typography)(({ theme }) => ({
  fontSize: '0.71875rem',
  color: theme.palette.text.secondary,
}));

export const StatDelta = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  color: theme.palette.text.disabled,
  fontVariantNumeric: 'tabular-nums',
  '& b': {
    fontWeight: 650,
    color: theme.palette.success.main,
  },
}));
