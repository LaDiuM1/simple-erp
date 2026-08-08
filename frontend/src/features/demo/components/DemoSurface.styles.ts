import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';

export const DemoLoginRoot = styled(Box)(({ theme }) => ({
  marginTop: '1.5rem',
  paddingTop: '1.25rem',
  borderTop: `1px solid ${theme.palette.divider}`,
}));

export const DemoTitleRow = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.75rem',
  marginBottom: '0.625rem',
});

export const DemoTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
}));

export const DemoBadge = styled('span')(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  borderRadius: 9999,
  padding: '0.1875rem 0.5rem',
  backgroundColor: theme.palette.primarySubtle,
  color: theme.palette.primary.dark,
  fontSize: '0.6875rem',
  fontWeight: 700,
}));

export const DemoNotice = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.secondary,
  fontSize: '0.8125rem',
  lineHeight: 1.55,
  whiteSpace: 'pre-line',
}));

export const AccountList = styled(Box)({
  display: 'grid',
  gap: '0.5rem',
  marginTop: '0.875rem',
});

export const AccountButton = styled(Button)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr auto',
  alignItems: 'center',
  justifyContent: 'stretch',
  textAlign: 'left',
  borderColor: theme.palette.divider,
  padding: '0.625rem 0.75rem',
  '&:hover': {
    borderColor: theme.palette.primary.light,
    backgroundColor: theme.palette.primarySubtle,
  },
}));

export const AccountText = styled('span')({
  display: 'flex',
  minWidth: 0,
  flexDirection: 'column',
  alignItems: 'flex-start',
});

export const AccountLabel = styled('span')(({ theme }) => ({
  color: theme.palette.text.primary,
  fontSize: '0.8125rem',
  fontWeight: 700,
}));

export const AccountDescription = styled('span')(({ theme }) => ({
  marginTop: 2,
  color: theme.palette.text.secondary,
  fontSize: '0.6875rem',
  lineHeight: 1.35,
}));

export const AccountLoginId = styled('span')(({ theme }) => ({
  color: theme.palette.primary.main,
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, monospace',
  fontSize: '0.75rem',
  fontWeight: 600,
}));

export const CountdownRow = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.75rem',
  marginTop: '0.875rem',
  padding: '0.625rem 0.75rem',
  backgroundColor: theme.palette.headerBg,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 6,
  color: theme.palette.text.secondary,
  fontSize: '0.75rem',
}));

export const CountdownValue = styled('strong')(({ theme }) => ({
  color: theme.palette.text.primary,
  fontVariantNumeric: 'tabular-nums',
  fontSize: '0.8125rem',
}));

export const BannerRoot = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'tone',
})<{ tone: 'info' | 'warning' | 'error' }>(({ theme, tone }) => {
  const colors = tone === 'error'
    ? { background: theme.palette.errorBg, border: theme.palette.errorBorder, foreground: theme.palette.error.dark }
    : tone === 'warning'
      ? { background: '#FFFBEB', border: '#FDE68A', foreground: '#92400E' }
      : { background: theme.palette.primarySubtle, border: theme.palette.primaryLight, foreground: theme.palette.primary.dark };
  return {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: '1rem',
    flexWrap: 'wrap',
    flexShrink: 0,
    minHeight: 42,
    padding: '0.5rem 1.5rem',
    backgroundColor: colors.background,
    borderBottom: `1px solid ${colors.border}`,
    color: colors.foreground,
    fontSize: '0.8125rem',
    [theme.breakpoints.down('sm')]: {
      alignItems: 'flex-start',
      padding: '0.625rem 1rem',
      gap: '0.375rem',
    },
  };
});

export const BannerMessage = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  minWidth: 0,
  '& > strong': {
    flexShrink: 0,
    whiteSpace: 'nowrap',
  },
  '& > span': {
    minWidth: 0,
  },
});

export const BannerMeta = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  whiteSpace: 'nowrap',
  fontVariantNumeric: 'tabular-nums',
});

export const MaintenanceRoot = styled(Box)(({ theme }) => ({
  minHeight: '100vh',
  display: 'grid',
  placeItems: 'center',
  padding: '1.5rem',
  background: theme.palette.loginGradient,
}));

export const MaintenanceCard = styled(Box)(({ theme }) => ({
  width: '100%',
  maxWidth: 480,
  padding: '2.5rem 2rem',
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 12,
  backgroundColor: theme.palette.background.paper,
  boxShadow: theme.shadows[4],
  textAlign: 'center',
}));

export const GuideRoot = styled(Box)(({ theme }) => ({
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 8,
  backgroundColor: theme.palette.background.paper,
}));

export const GuideHeader = styled(Button)(({ theme }) => ({
  width: '100%',
  justifyContent: 'space-between',
  padding: '0.75rem 1rem',
  color: theme.palette.text.primary,
  fontSize: '0.875rem',
  fontWeight: 700,
}));

export const GuideList = styled('ol')(({ theme }) => ({
  display: 'grid',
  gap: '0.5rem',
  margin: 0,
  padding: '0 1.25rem 1rem 2.5rem',
  color: theme.palette.text.secondary,
  fontSize: '0.8125rem',
  lineHeight: 1.5,
}));
