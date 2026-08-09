import Box from '@mui/material/Box';
import LinearProgress, { linearProgressClasses } from '@mui/material/LinearProgress';
import { styled } from '@mui/material/styles';

export const PageLoadingBoundary = styled(Box)(({ theme }) => ({
  position: 'relative',
  minHeight: '20rem',
  width: '100%',
  [theme.breakpoints.down('sm')]: {
    minHeight: '16rem',
  },
}));

export const PageLoadingProgress = styled(LinearProgress)({
  position: 'absolute',
  inset: '0 0 auto',
  height: 2,
  '@media (prefers-reduced-motion: reduce)': {
    [`& .${linearProgressClasses.bar}`]: {
      animation: 'none',
      transform: 'scaleX(1)',
    },
  },
});
