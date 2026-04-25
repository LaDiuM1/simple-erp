import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const FormRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  [theme.breakpoints.up('sm')]: {
    margin: '-2rem',
  },
}));

export const FormSurface = styled('form')(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  [theme.breakpoints.up('md')]: {
    padding: '2rem',
  },
}));

export const SectionSurface = styled(Box)(({ theme }) => ({
  width: '100%',
  maxWidth: 960,
  marginLeft: 'auto',
  marginRight: 'auto',
  '& + &': {
    marginTop: '1rem',
    paddingTop: '1rem',
    borderTop: `1px dashed ${theme.palette.divider}`,
  },
  [theme.breakpoints.up('md')]: {
    '& + &': {
      marginTop: '1.25rem',
      paddingTop: '1.25rem',
    },
  },
}));

export const SectionHeader = styled(Box)({
  display: 'flex',
  alignItems: 'flex-start',
  gap: '0.75rem',
  marginBottom: '1.25rem',
});

export const SectionIcon = styled(Box)(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: 32,
  height: 32,
  borderRadius: 8,
  color: theme.palette.primary.main,
  backgroundColor: theme.palette.primarySubtle,
  flexShrink: 0,
}));

export const SectionTitleBox = styled(Box)({
  minWidth: 0,
  paddingTop: '0.0625rem',
});

export const SectionTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.005em',
  lineHeight: 1.3,
}));

export const SectionDescription = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  marginTop: '0.125rem',
}));

export const FormGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '1rem',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: '1fr 1fr',
  },
}));
