import { styled } from '@mui/material/styles';
import { Box } from '@mui/material';

export const ErrorIconBox = styled(Box)(({ theme }) => ({
  width: 52,
  height: 52,
  borderRadius: '50%',
  backgroundColor: theme.palette.errorBg,
  border: `1.5px solid ${theme.palette.errorBorder}`,
  color: theme.palette.error.main,
  fontSize: '1.5rem',
  fontWeight: 700,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
}));
