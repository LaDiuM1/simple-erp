import { styled, alpha } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const TrendRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 10,
  padding: '1rem 1.125rem 0.875rem',
  display: 'flex',
  flexDirection: 'column',
  minWidth: 0,
}));

export const TrendHead = styled(Box)({
  display: 'flex',
  alignItems: 'flex-start',
  justifyContent: 'space-between',
  gap: '0.75rem',
});

export const TrendLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  fontWeight: 700,
  letterSpacing: '0.1em',
  textTransform: 'uppercase',
  color: theme.palette.text.secondary,
}));

export const TrendCaption = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.disabled,
  flexShrink: 0,
}));

export const TrendValueRow = styled(Box)({
  display: 'flex',
  alignItems: 'baseline',
  gap: '0.5rem',
  marginTop: '0.5rem',
  flexWrap: 'wrap',
});

export const TrendValue = styled(Typography)(({ theme }) => ({
  fontSize: '2.125rem',
  fontWeight: 780,
  color: theme.palette.text.primary,
  letterSpacing: '-0.035em',
  lineHeight: 1,
  fontVariantNumeric: 'tabular-nums',
}));

export const TrendUnit = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
}));

export const TrendDelta = styled('span', {
  shouldForwardProp: (prop) => prop !== 'negative',
})<{ negative?: boolean }>(({ theme, negative }) => ({
  fontSize: '0.78125rem',
  fontWeight: 650,
  color: negative ? theme.palette.error.main : theme.palette.success.main,
  fontVariantNumeric: 'tabular-nums',
}));

/** 바 차트 영역 — flex 컬럼들로 responsive 하게 (SVG 왜곡 없음). */
export const ChartArea = styled(Box)({
  flex: 1,
  display: 'flex',
  alignItems: 'stretch',
  gap: '0.625rem',
  marginTop: '0.875rem',
  minHeight: 150,
});

export const BarColumn = styled(Box)({
  flex: 1,
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  gap: '0.375rem',
  minWidth: 0,
});

export const BarTrack = styled(Box)(({ theme }) => ({
  flex: 1,
  width: '100%',
  display: 'flex',
  alignItems: 'flex-end',
  justifyContent: 'center',
  borderBottom: `1px solid ${theme.palette.divider}`,
}));

export const Bar = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'current',
})<{ current?: boolean }>(({ theme, current }) => ({
  width: '62%',
  maxWidth: 44,
  minHeight: 3,
  borderRadius: '4px 4px 0 0',
  backgroundColor: current
    ? theme.palette.primary.main
    : alpha(theme.palette.primary.main, 0.22),
  transition: 'background-color 0.15s ease',
  '&:hover': {
    backgroundColor: current
      ? theme.palette.primary.dark
      : alpha(theme.palette.primary.main, 0.38),
  },
}));

export const BarLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.65625rem',
  color: theme.palette.text.disabled,
  fontVariantNumeric: 'tabular-nums',
  whiteSpace: 'nowrap',
}));
