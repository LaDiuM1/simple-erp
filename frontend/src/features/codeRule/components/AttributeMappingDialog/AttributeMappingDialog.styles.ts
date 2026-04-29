import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { styled } from '@mui/material/styles';

export const Section = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
});

export const Field = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
});

export const FieldLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  fontWeight: 700,
  color: theme.palette.text.secondary,
  textTransform: 'uppercase',
  letterSpacing: '0.06em',
  marginBottom: '0.375rem',
}));
