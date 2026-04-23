import { styled } from '@mui/material/styles';
import { Box } from '@mui/material';

export const ScreenContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  minHeight: '100vh',
  gap: theme.spacing(2),
  backgroundColor: theme.palette.background.default,
  padding: theme.spacing(4),
  textAlign: 'center',
}));
