import Box from '@mui/material/Box';
import { styled } from '@mui/material/styles';

export const PageLoadingBoundary = styled(Box)(({ theme }) => ({
  position: 'relative',
  minHeight: '20rem',
  width: '100%',
  [theme.breakpoints.down('sm')]: {
    minHeight: '16rem',
  },
}));

export const PageLoadingStatus = styled('span')({
  position: 'absolute',
  width: 1,
  height: 1,
  padding: 0,
  margin: -1,
  overflow: 'hidden',
  clip: 'rect(0 0 0 0)',
  whiteSpace: 'nowrap',
  border: 0,
});
