import { styled } from '@mui/material/styles';
import { Box } from '@mui/material';

export const LoginContainer = styled(Box)({
  minHeight: '100vh',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  background: 'linear-gradient(135deg, #eef2ff 0%, #f0f9ff 50%, #f0fdf4 100%)',
  padding: '1rem',
});

export const LoginCard = styled(Box)(({ theme }) => ({
  width: '100%',
  maxWidth: 420,
  backgroundColor: theme.palette.background.paper,
  borderRadius: 16,
  boxShadow: theme.shadows[4],
  padding: '2.5rem 2rem',
  [theme.breakpoints.down('sm')]: {
    padding: '2rem 1.5rem',
  },
}));

export const LogoBox = styled(Box)(({ theme }) => ({
  width: 56,
  height: 56,
  backgroundColor: theme.palette.primary.main,
  borderRadius: 12,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontSize: '1rem',
  fontWeight: 800,
  color: 'white',
  margin: '0 auto 1rem',
  letterSpacing: '-0.5px',
  boxShadow: '0 4px 14px rgb(79 70 229 / 0.35)',
}));
