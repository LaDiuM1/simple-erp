import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { alpha, styled } from '@mui/material/styles';

export const PerformanceSummary = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 8,
  backgroundColor: theme.palette.headerBg,
  [theme.breakpoints.down('sm')]: { gridTemplateColumns: '1fr' },
}));

export const PerformanceMetric = styled(Box)(({ theme }) => ({
  padding: '0.75rem 0.875rem',
  minWidth: 0,
  '& + &': {
    borderLeft: `1px solid ${theme.palette.divider}`,
    [theme.breakpoints.down('sm')]: {
      borderLeft: 0,
      borderTop: `1px solid ${theme.palette.divider}`,
    },
  },
}));

export const MetricLabel = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.secondary,
  fontSize: '0.75rem',
}));

export const MetricValue = styled(Typography)(({ theme }) => ({
  marginTop: '0.1875rem',
  color: theme.palette.text.primary,
  fontSize: '1.125rem',
  fontWeight: 700,
  fontVariantNumeric: 'tabular-nums',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
}));

export const TrendHeader = styled(Box)(({ theme }) => ({
  display: 'flex',
  justifyContent: 'space-between',
  color: theme.palette.text.secondary,
  fontSize: '0.6875rem',
  fontWeight: 600,
}));

export const TrendList = styled('ul')({
  display: 'grid',
  gap: '0.625rem',
  listStyle: 'none',
  margin: 0,
  padding: 0,
});

export const TrendRow = styled('li')(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '4.75rem minmax(4rem, 1fr) 7.5rem auto',
  alignItems: 'center',
  gap: '0.625rem',
  minWidth: 0,
  [theme.breakpoints.down('sm')]: {
    gridTemplateColumns: '4.5rem minmax(3rem, 1fr) 5.75rem',
  },
}));

export const TrendMonth = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.secondary,
  fontSize: '0.75rem',
  fontVariantNumeric: 'tabular-nums',
  whiteSpace: 'nowrap',
}));

export const TrendBar = styled(Box)(({ theme }) => ({
  height: 8,
  overflow: 'hidden',
  borderRadius: 9999,
  backgroundColor: theme.palette.primarySubtle,
}));

export const TrendBarFill = styled(Box, {
  shouldForwardProp: (prop) => prop !== '$width',
})<{ $width: number }>(({ theme, $width }) => ({
  width: `${$width}%`,
  height: '100%',
  borderRadius: 'inherit',
  backgroundColor: theme.palette.primary.main,
  transition: 'width 0.2s ease',
}));

export const TrendAmount = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.primary,
  fontSize: '0.8125rem',
  fontWeight: 600,
  fontVariantNumeric: 'tabular-nums',
  textAlign: 'right',
  whiteSpace: 'nowrap',
}));

export const TrendCount = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.secondary,
  fontSize: '0.75rem',
  fontVariantNumeric: 'tabular-nums',
  whiteSpace: 'nowrap',
  [theme.breakpoints.down('sm')]: { display: 'none' },
}));

export const CollectionPanel = styled(Box)(({ theme }) => ({
  paddingTop: '0.875rem',
  borderTop: `1px solid ${theme.palette.divider}`,
}));

export const CollectionHeader = styled(Box)({
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'baseline',
  gap: '0.75rem',
});

export const CollectionLabel = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.primary,
  fontSize: '0.8125rem',
  fontWeight: 600,
}));

export const CollectionRate = styled(Typography)(({ theme }) => ({
  color: theme.palette.primary.dark,
  fontSize: '0.875rem',
  fontWeight: 700,
  fontVariantNumeric: 'tabular-nums',
}));

export const ProgressTrack = styled(Box)(({ theme }) => ({
  height: 7,
  marginTop: '0.5rem',
  overflow: 'hidden',
  borderRadius: 9999,
  backgroundColor: alpha(theme.palette.text.disabled, 0.2),
}));

export const ProgressFill = styled(Box, {
  shouldForwardProp: (prop) => prop !== '$width',
})<{ $width: number }>(({ theme, $width }) => ({
  width: `${$width}%`,
  height: '100%',
  borderRadius: 'inherit',
  backgroundColor: theme.palette.primary.main,
}));

export const CollectionValues = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
  gap: '0.75rem',
  marginTop: '0.625rem',
  [theme.breakpoints.down('sm')]: { gridTemplateColumns: '1fr' },
}));

export const CollectionValue = styled(Box, {
  shouldForwardProp: (prop) => prop !== '$warning',
})<{ $warning?: boolean }>(({ theme, $warning }) => ({
  display: 'flex',
  justifyContent: 'space-between',
  gap: '0.375rem',
  minWidth: 0,
  color: $warning ? theme.palette.warning.main : theme.palette.text.secondary,
  fontSize: '0.75rem',
  '& strong': {
    color: $warning ? theme.palette.warning.main : theme.palette.text.primary,
    fontWeight: 600,
    fontVariantNumeric: 'tabular-nums',
    whiteSpace: 'nowrap',
  },
}));
