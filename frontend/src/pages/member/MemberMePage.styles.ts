import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const PageRoot = styled(Box)({
  maxWidth: 720,
});

export const PageTitle = styled(Typography)(({ theme }) => ({
  fontSize: '1.375rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
  marginBottom: '1.5rem',
}));
