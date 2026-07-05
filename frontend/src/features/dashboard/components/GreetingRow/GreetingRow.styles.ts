import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const GreetingRoot = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '1rem',
  padding: '0 0.125rem',
  [theme.breakpoints.down('sm')]: {
    flexDirection: 'column',
    alignItems: 'stretch',
    gap: '0.75rem',
  },
}));

export const GreetingTitle = styled(Typography)(({ theme }) => ({
  fontSize: '1.375rem',
  fontWeight: 740,
  color: theme.palette.text.primary,
  letterSpacing: '-0.026em',
  lineHeight: 1.25,
}));

export const GreetingSub = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  marginTop: '0.1875rem',
}));

export const ActionArea = styled(Box)({
  display: 'flex',
  gap: '0.5rem',
  flexShrink: 0,
});
