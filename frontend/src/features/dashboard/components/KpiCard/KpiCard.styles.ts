import { styled, alpha } from '@mui/material/styles';
import type { CSSObject, Theme } from '@mui/material/styles';

const kpiLayout = (theme: Theme): CSSObject => ({
  appearance: 'none',
  fontFamily: 'inherit',
  textAlign: 'left',
  display: 'flex',
  alignItems: 'center',
  gap: '0.875rem',
  minWidth: 0,
  padding: '0.875rem 1rem',
  borderRadius: 10,
  border: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.paper,
  transition: 'border-color 0.15s ease, box-shadow 0.15s ease',
});

export const KpiRoot = styled('div')(({ theme }) => kpiLayout(theme));

export const KpiButton = styled('button')(({ theme }) => ({
  ...kpiLayout(theme),
  cursor: 'pointer',
  '&:hover': {
    borderColor: theme.palette.primaryLight,
    boxShadow: `0 4px 14px -6px ${alpha(theme.palette.primary.main, 0.18)}`,
  },
  '&:focus-visible': {
    outline: `2px solid ${theme.palette.primary.main}`,
    outlineOffset: 2,
  },
}));

export const KpiIcon = styled('span')(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: 36,
  height: 36,
  borderRadius: 9,
  flexShrink: 0,
  color: theme.palette.primary.main,
  backgroundColor: theme.palette.primarySubtle,
}));

export const KpiBody = styled('span')({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.125rem',
  minWidth: 0,
});

export const KpiLabel = styled('span')(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  fontWeight: 500,
  letterSpacing: '0.02em',
  textTransform: 'uppercase',
}));

export const KpiValueRow = styled('span')({
  display: 'flex',
  alignItems: 'baseline',
  gap: '0.25rem',
  marginTop: '0.125rem',
});

export const KpiValue = styled('span')(({ theme }) => ({
  fontSize: '1.5rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
  letterSpacing: '-0.025em',
  lineHeight: 1.1,
  fontVariantNumeric: 'tabular-nums',
}));

export const KpiUnit = styled('span')(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  fontWeight: 500,
}));

export const KpiSuffix = styled('span')(({ theme }) => ({
  fontSize: '0.6875rem',
  color: theme.palette.text.disabled,
  marginTop: '0.125rem',
}));

export const KpiArrow = styled('span')(({ theme }) => ({
  alignSelf: 'flex-start',
  display: 'inline-flex',
  color: theme.palette.text.disabled,
  marginLeft: 'auto',
  '& > svg': { fontSize: 17 },
}));
