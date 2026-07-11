import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

export const PageRoot = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '1.125rem',
  maxWidth: 960,
  [theme.breakpoints.down('sm')]: { gap: '0.75rem' },
}));

export const MonthlyFilterGroup = styled(Box)({
  display: 'flex',
  gap: '0.5rem',
  alignItems: 'center',
});
