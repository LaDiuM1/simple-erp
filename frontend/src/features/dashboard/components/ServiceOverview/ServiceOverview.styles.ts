import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { styled } from '@mui/material/styles';

export const ServiceSummary = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 8,
  backgroundColor: theme.palette.headerBg,
}));

export const ServiceMetric = styled(Box)(({ theme }) => ({
  padding: '0.625rem 0.75rem',
  minWidth: 0,
  '& + &': { borderLeft: `1px solid ${theme.palette.divider}` },
}));

export const ServiceMetricLabel = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.secondary,
  fontSize: '0.6875rem',
}));

export const ServiceMetricValue = styled(Typography)(({ theme }) => ({
  marginTop: '0.125rem',
  color: theme.palette.text.primary,
  fontSize: '0.9375rem',
  fontWeight: 700,
  fontVariantNumeric: 'tabular-nums',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
}));

export const SubHeading = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.secondary,
  fontSize: '0.6875rem',
  fontWeight: 700,
}));

export const ServiceTypeList = styled('ul')({
  display: 'grid',
  gap: '0.5rem',
  margin: 0,
  padding: 0,
  listStyle: 'none',
});

export const ServiceTypeRow = styled('li')(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '4.75rem minmax(2.5rem, 1fr) auto 6.5rem',
  alignItems: 'center',
  gap: '0.5rem',
  minWidth: 0,
  [theme.breakpoints.down('sm')]: {
    gridTemplateColumns: 'minmax(0, 1fr) auto',
    gridTemplateAreas: '"label count" "bar expense"',
    rowGap: '0.375rem',
  },
}));

export const ServiceLabel = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.primary,
  fontSize: '0.75rem',
  fontWeight: 500,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
  [theme.breakpoints.down('sm')]: { gridArea: 'label' },
}));

export const ServiceBar = styled(Box)(({ theme }) => ({
  height: 6,
  overflow: 'hidden',
  borderRadius: 9999,
  backgroundColor: theme.palette.primarySubtle,
  [theme.breakpoints.down('sm')]: { gridArea: 'bar' },
}));

export const ServiceBarFill = styled(Box, {
  shouldForwardProp: (prop) => prop !== '$width',
})<{ $width: number }>(({ theme, $width }) => ({
  width: `${$width}%`,
  height: '100%',
  borderRadius: 'inherit',
  backgroundColor: theme.palette.primary.main,
}));

export const ServiceCount = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.secondary,
  fontSize: '0.75rem',
  fontVariantNumeric: 'tabular-nums',
  whiteSpace: 'nowrap',
  [theme.breakpoints.down('sm')]: { gridArea: 'count' },
}));

export const ServiceExpense = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.primary,
  fontSize: '0.75rem',
  fontWeight: 600,
  fontVariantNumeric: 'tabular-nums',
  textAlign: 'right',
  whiteSpace: 'nowrap',
  [theme.breakpoints.down('sm')]: { gridArea: 'expense' },
}));

export const EngineerList = styled('ul')({
  display: 'grid',
  gap: '0.375rem',
  margin: 0,
  padding: 0,
  listStyle: 'none',
});

export const EngineerRow = styled('li')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.75rem',
  paddingTop: '0.375rem',
  borderTop: `1px solid ${theme.palette.divider}`,
}));

export const EngineerName = styled(Typography)(({ theme }) => ({
  minWidth: 0,
  overflow: 'hidden',
  color: theme.palette.text.secondary,
  fontSize: '0.75rem',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
}));

export const EngineerAmount = styled(Typography)(({ theme }) => ({
  flexShrink: 0,
  color: theme.palette.text.primary,
  fontSize: '0.75rem',
  fontWeight: 600,
  fontVariantNumeric: 'tabular-nums',
}));
