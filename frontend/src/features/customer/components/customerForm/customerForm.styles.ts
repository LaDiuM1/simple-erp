import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';

export const CreateRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  [theme.breakpoints.up('sm')]: {
    margin: '-2rem',
  },
}));

export const CreateForm = styled('form')(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  [theme.breakpoints.up('md')]: {
    padding: '2rem',
  },
}));

export const FieldGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '1rem',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: '1fr 1fr',
  },
}));

export const FieldFull = styled(Box)(({ theme }) => ({
  [theme.breakpoints.up('md')]: {
    gridColumn: '1 / -1',
  },
}));

export const AddressSearchRow = styled(Box)({
  display: 'flex',
  gap: '0.5rem',
  alignItems: 'stretch',
});

export const AddressSearchButton = styled(Button)(({ theme }) => ({
  paddingLeft: '0.875rem',
  paddingRight: '0.875rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
  borderColor: theme.palette.divider,
  flexShrink: 0,
  whiteSpace: 'nowrap',
  '&:hover': {
    borderColor: theme.palette.primary.main,
    color: theme.palette.primary.main,
    backgroundColor: theme.palette.primarySubtle,
  },
}));
