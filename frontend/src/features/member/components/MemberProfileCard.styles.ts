import { styled } from '@mui/material/styles';
import { Box } from '@mui/material';

export const ProfileCard = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  borderRadius: 12,
  boxShadow: theme.shadows[2],
  border: `1px solid ${theme.palette.divider}`,
  overflow: 'hidden',
}));

export const ProfileHeader = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: '1.25rem',
  padding: '2rem',
  background: 'linear-gradient(135deg, #EEF2FF 0%, #F0F9FF 100%)',
  borderBottom: `1px solid ${theme.palette.divider}`,
}));

export const AvatarBox = styled(Box)(({ theme }) => ({
  width: 72,
  height: 72,
  backgroundColor: theme.palette.primary.main,
  borderRadius: '50%',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontSize: '1.75rem',
  fontWeight: 700,
  color: 'white',
  flexShrink: 0,
  boxShadow: '0 4px 12px rgb(79 70 229 / 0.3)',
}));

export const InfoGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(2, 1fr)',
  borderBottom: `1px solid ${theme.palette.divider}`,
  [theme.breakpoints.down('sm')]: {
    gridTemplateColumns: '1fr',
  },
}));

export const InfoItem = styled(Box)(({ theme }) => ({
  padding: '1.25rem 1.5rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  borderRight: `1px solid ${theme.palette.divider}`,
  '&:nth-of-type(2n)': { borderRight: 'none' },
  '&:nth-last-of-type(-n+2)': { borderBottom: 'none' },
  [theme.breakpoints.down('sm')]: {
    borderRight: 'none',
    '&:last-of-type': { borderBottom: 'none' },
    '&:nth-last-of-type(-n+2)': { borderBottom: `1px solid ${theme.palette.divider}` },
  },
}));

export const PermItem = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  padding: '0.625rem 0.875rem',
  backgroundColor: theme.palette.background.default,
  borderRadius: 6,
  border: `1px solid ${theme.palette.divider}`,
}));
