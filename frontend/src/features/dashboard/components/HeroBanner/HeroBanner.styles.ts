import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const HeroRoot = styled(Box)(({ theme }) => ({
  borderRadius: 10,
  padding: '1.125rem 1.375rem',
  backgroundColor: theme.palette.headerBg,
  border: `1px solid ${theme.palette.divider}`,
  display: 'grid',
  gridTemplateColumns: '1fr auto',
  alignItems: 'center',
  gap: '1.5rem',
  [theme.breakpoints.down('md')]: {
    gridTemplateColumns: '1fr',
    padding: '1rem 1.125rem',
    gap: '0.875rem',
  },
}));

export const HeroLeft = styled(Box)({
  minWidth: 0,
});

export const HeroGreeting = styled(Typography)(({ theme }) => ({
  fontSize: '1.25rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
  letterSpacing: '-0.02em',
  lineHeight: 1.3,
  '& strong': {
    fontWeight: 700,
    color: theme.palette.primary.main,
  },
  [theme.breakpoints.down('md')]: { fontSize: '1.125rem' },
}));

export const HeroSubtext = styled(Box)(({ theme }) => ({
  fontSize: '0.875rem',
  color: theme.palette.text.secondary,
  marginTop: '0.375rem',
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  flexWrap: 'wrap',
}));

export const HeroBadge = styled('span')(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.375rem',
  padding: '0.1875rem 0.5rem',
  fontSize: '0.75rem',
  fontWeight: 600,
  borderRadius: 9999,
  backgroundColor: theme.palette.primarySubtle,
  border: `1px solid ${theme.palette.primaryLight}`,
  color: theme.palette.primary.dark,
}));

export const HeroDivider = styled('span')(({ theme }) => ({
  width: 3,
  height: 3,
  borderRadius: '50%',
  backgroundColor: theme.palette.text.disabled,
  display: 'inline-block',
}));

export const HeroRight = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: '0.625rem',
  color: theme.palette.text.secondary,
  '& > svg': {
    color: theme.palette.primary.main,
    fontSize: 20,
  },
}));

export const DateMeta = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'flex-end',
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  lineHeight: 1.35,
  [theme.breakpoints.down('md')]: { alignItems: 'flex-start' },
}));

export const DateLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  fontWeight: 700,
  color: theme.palette.text.disabled,
}));
