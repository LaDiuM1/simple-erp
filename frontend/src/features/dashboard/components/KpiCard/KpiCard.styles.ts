import { styled, alpha } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const KpiRoot = styled('button')(({ theme }) => ({
  appearance: 'none',
  cursor: 'pointer',
  fontFamily: 'inherit',
  textAlign: 'left',
  display: 'flex',
  alignItems: 'center',
  gap: '1rem',
  padding: '0.875rem 1.375rem',
  borderRadius: 12,
  border: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.paper,
  transition: 'border-color 0.15s ease, box-shadow 0.15s ease',
  '&:hover': {
    borderColor: theme.palette.primaryLight,
    boxShadow: `0 4px 14px -6px ${alpha(theme.palette.primary.main, 0.18)}`,
  },
}));

export const KpiIcon = styled(Box)(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: 40,
  height: 40,
  borderRadius: 10,
  flexShrink: 0,
  color: theme.palette.primary.main,
  backgroundColor: theme.palette.primarySubtle,
}));

export const KpiBody = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.125rem',
  minWidth: 0,
});

export const KpiLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  fontWeight: 500,
  letterSpacing: '0.02em',
  textTransform: 'uppercase',
}));

export const KpiValueRow = styled(Box)({
  display: 'flex',
  alignItems: 'baseline',
  gap: '0.25rem',
  marginTop: '0.125rem',
});

export const KpiValue = styled(Typography)(({ theme }) => ({
  fontSize: '1.625rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
  letterSpacing: '-0.025em',
  lineHeight: 1.1,
  fontVariantNumeric: 'tabular-nums',
}));

export const KpiUnit = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  fontWeight: 500,
}));

export const KpiSuffix = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  color: theme.palette.text.disabled,
  marginTop: '0.125rem',
}));
