import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const HeroRoot = styled(Box)(({ theme }) => ({
  borderRadius: 12,
  padding: '1.375rem 2rem',
  background: theme.palette.profileGradient,
  border: `1px solid ${theme.palette.divider}`,
  display: 'grid',
  gridTemplateColumns: '1fr auto',
  alignItems: 'center',
  gap: '2rem',
  [theme.breakpoints.down('md')]: {
    gridTemplateColumns: '1fr',
    padding: '1.125rem 1.5rem',
    gap: '0.875rem',
  },
}));

export const HeroLeft = styled(Box)({
  minWidth: 0,
});

export const HeroEyebrow = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  fontWeight: 600,
  color: theme.palette.text.disabled,
  letterSpacing: '0.08em',
  textTransform: 'uppercase',
  marginBottom: '0.5rem',
}));

export const HeroGreeting = styled(Typography)(({ theme }) => ({
  fontSize: '1.625rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
  letterSpacing: '-0.02em',
  lineHeight: 1.3,
  '& strong': {
    fontWeight: 700,
    color: theme.palette.primary.main,
  },
  [theme.breakpoints.down('md')]: { fontSize: '1.375rem' },
}));

export const HeroSubtext = styled(Box)(({ theme }) => ({
  fontSize: '0.875rem',
  color: theme.palette.text.secondary,
  marginTop: '0.5rem',
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
  flexDirection: 'column',
  alignItems: 'flex-end',
  gap: '0.375rem',
  [theme.breakpoints.down('md')]: { alignItems: 'flex-start' },
}));

export const ClockText = styled(Typography)(({ theme }) => ({
  fontSize: '2.25rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
  letterSpacing: '-0.025em',
  lineHeight: 1,
  fontVariantNumeric: 'tabular-nums',
}));

export const ClockDate = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  letterSpacing: '-0.005em',
}));
