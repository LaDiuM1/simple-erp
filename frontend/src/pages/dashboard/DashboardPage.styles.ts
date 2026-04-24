import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const PageRoot = styled(Box)({
  maxWidth: 900,
});

export const GreetingBlock = styled(Box)({
  marginBottom: '2rem',
});

export const GreetingTitle = styled(Typography)(({ theme }) => ({
  fontSize: '1.625rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
  marginBottom: '0.375rem',
}));

export const GreetingDate = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  color: theme.palette.text.secondary,
}));

export const DashboardGrid = styled(Box)({
  display: 'grid',
  gridTemplateColumns: 'repeat(4, 1fr)',
  gap: '1rem',
  '@media (max-width: 900px)': {
    gridTemplateColumns: 'repeat(2, 1fr)',
  },
  '@media (max-width: 480px)': {
    gridTemplateColumns: '1fr',
  },
});

export const DashboardCard = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 12,
  padding: '1.375rem 1.25rem',
  boxShadow: theme.shadows[1],
  transition: 'box-shadow 0.15s, transform 0.15s',
  '&:hover': {
    boxShadow: theme.shadows[3],
    transform: 'translateY(-2px)',
  },
}));

export const CardLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
  marginBottom: '0.625rem',
  textTransform: 'uppercase',
  letterSpacing: '0.06em',
}));

export const CardValue = styled(Typography)(({ theme }) => ({
  fontSize: '1.25rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
}));
