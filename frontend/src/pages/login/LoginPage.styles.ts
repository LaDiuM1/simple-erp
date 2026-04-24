import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import InputBase from '@mui/material/InputBase';
import Typography from '@mui/material/Typography';

export const LoginContainer = styled(Box)({
  minHeight: '100vh',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  background: 'linear-gradient(135deg, #eff6ff 0%, #f0f9ff 50%, #f0fdf4 100%)',
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

/** 로고 + 타이틀 묶음 wrapper */
export const LoginHeader = styled(Box)({
  textAlign: 'center',
  marginBottom: '2rem',
});

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
  boxShadow: '0 4px 14px rgb(59 130 246 / 0.35)',
}));

export const AppTitle = styled(Typography)(({ theme }) => ({
  fontSize: '1.5rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
  marginBottom: '0.375rem',
}));

export const AppSubtitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.875rem',
  color: theme.palette.text.secondary,
}));

export const LoginForm = styled('form')({
  display: 'flex',
  flexDirection: 'column',
  gap: '1.125rem',
});

export const FormField = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.4375rem',
});

export const FieldLabel = styled('label')(({ theme }) => ({
  fontSize: '0.875rem',
  fontWeight: 500,
  color: theme.palette.text.primary,
}));

export const StyledInput = styled(InputBase)(({ theme }) => ({
  borderRadius: 8,
  backgroundColor: theme.palette.background.paper,
  border: `1.5px solid ${theme.palette.divider}`,
  boxShadow: '0 0 0 0 rgba(59, 130, 246, 0)',
  transition: 'border-color 0.15s, box-shadow 0.15s',
  '&.Mui-focused': {
    borderColor: theme.palette.primary.light,
    boxShadow: '0 0 0 3px rgba(59, 130, 246, 0.12)',
  },
  '& input': {
    padding: '0.6875rem 0.875rem',
    fontSize: '0.9375rem',
    color: theme.palette.text.primary,
    '&::placeholder': {
      color: theme.palette.text.disabled,
      opacity: 1,
    },
  },
}));

export const ErrorBox = styled(Box)(({ theme }) => ({
  padding: '0.6875rem 0.875rem',
  backgroundColor: theme.palette.error.light,
  border: `1px solid ${theme.palette.errorBorder}`,
  borderRadius: 8,
  fontSize: '0.875rem',
  color: theme.palette.error.main,
}));

export const SubmitButton = styled(Button)({
  marginTop: '0.25rem',
  paddingTop: '0.75rem',
  paddingBottom: '0.75rem',
  fontSize: '0.9375rem',
  fontWeight: 600,
  '&:active': { transform: 'scale(0.99)' },
});
