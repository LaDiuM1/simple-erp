import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const DashboardRoot = styled(Box)(({ theme }) => ({
  maxWidth: 960,
  display: 'flex',
  flexDirection: 'column',
  gap: '2rem',
  [theme.breakpoints.down('sm')]: {
    gap: '1.5rem',
  },
}));

export const WelcomeHeader = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'flex-start',
  justifyContent: 'space-between',
  gap: '1rem',
  [theme.breakpoints.down('sm')]: {
    flexDirection: 'column',
    alignItems: 'flex-start',
  },
}));

export const WelcomeText = styled(Box)({
  minWidth: 0,
});

export const WelcomeGreeting = styled(Typography)(({ theme }) => ({
  fontSize: '1.75rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.02em',
  lineHeight: 1.3,
  marginBottom: '0.5rem',
  '& strong': { fontWeight: 700 },
  [theme.breakpoints.down('sm')]: {
    fontSize: '1.375rem',
  },
}));

export const WelcomeDate = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  color: theme.palette.text.secondary,
  letterSpacing: '-0.005em',
}));

export const RoleBadge = styled(Box)(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.375rem',
  padding: '0.3125rem 0.75rem',
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: theme.palette.primary.main,
  backgroundColor: theme.palette.primarySubtle,
  border: `1px solid ${theme.palette.primaryLight}`,
  borderRadius: 9999,
  flexShrink: 0,
  marginTop: '0.375rem',
}));

export const SectionSurface = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 12,
  padding: '1.5rem',
  [theme.breakpoints.down('sm')]: {
    padding: '1.25rem',
  },
}));

export const SectionHeader = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  marginBottom: '1.125rem',
});

export const SectionTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.005em',
}));

export const SectionCount = styled('span')(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: theme.palette.text.disabled,
  marginLeft: '0.5rem',
}));

export const ProfileGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
  gap: '1rem 2rem',
  [theme.breakpoints.down('sm')]: {
    gridTemplateColumns: '1fr',
    gap: '0.75rem',
  },
}));

export const ProfileRow = styled(Box)({
  display: 'flex',
  alignItems: 'baseline',
  gap: '1rem',
  minWidth: 0,
});

export const ProfileLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  width: '4rem',
  flexShrink: 0,
}));

export const ProfileValue = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 500,
  color: theme.palette.text.primary,
  minWidth: 0,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
}));

export const ShortcutList = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
  gap: '0.625rem',
  [theme.breakpoints.down('sm')]: {
    gridTemplateColumns: '1fr',
  },
}));

export const ShortcutArrow = styled('span')(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  color: theme.palette.text.disabled,
  flexShrink: 0,
  transition: 'color 0.12s, transform 0.12s',
}));

export const ShortcutItem = styled('button')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: '0.875rem',
  padding: '0.875rem 1rem',
  background: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 10,
  cursor: 'pointer',
  textAlign: 'left',
  fontFamily: 'inherit',
  transition: 'background-color 0.12s, border-color 0.12s',
  '&:hover': {
    backgroundColor: theme.palette.primarySubtle,
    borderColor: theme.palette.primaryLight,
  },
  [`&:hover ${ShortcutArrow}`]: {
    color: theme.palette.primary.main,
    transform: 'translateX(2px)',
  },
}));

export const ShortcutIcon = styled(Box)(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: 36,
  height: 36,
  borderRadius: 8,
  color: theme.palette.primary.main,
  backgroundColor: theme.palette.primarySubtle,
  flexShrink: 0,
}));

export const ShortcutBody = styled(Box)({
  flex: 1,
  minWidth: 0,
  display: 'flex',
  flexDirection: 'column',
  gap: '0.125rem',
});

export const ShortcutName = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.005em',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
}));

export const ShortcutPath = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
}));

export const ShortcutEmpty = styled(Box)(({ theme }) => ({
  padding: '2rem',
  textAlign: 'center',
  fontSize: '0.875rem',
  color: theme.palette.text.secondary,
  border: `1px dashed ${theme.palette.divider}`,
  borderRadius: 10,
}));
