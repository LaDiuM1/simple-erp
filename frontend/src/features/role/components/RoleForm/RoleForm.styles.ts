import Box from '@mui/material/Box';
import { styled } from '@mui/material/styles';

export const FormRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  [theme.breakpoints.up('sm')]: { margin: '-2rem' },
}));

export const FormSurface = styled('form')(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '1.5rem',
  [theme.breakpoints.up('md')]: { padding: '2rem' },
}));

export const ContentBox = styled(Box)({
  width: '100%',
  maxWidth: 720,
  marginLeft: 'auto',
  marginRight: 'auto',
  display: 'flex',
  flexDirection: 'column',
  gap: '1.25rem',
});

export const FieldsGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '1rem',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: '1fr 1fr',
  },
}));

export const FieldFull = styled(Box)(({ theme }) => ({
  [theme.breakpoints.up('md')]: { gridColumn: '1 / -1' },
}));
