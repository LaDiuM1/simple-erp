import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

export const DetailRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '1.25rem',
  [theme.breakpoints.up('sm')]: {
    margin: '-2rem',
    gap: '1.5rem',
  },
}));

export const SectionRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  padding: '1rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.875rem',
  [theme.breakpoints.up('md')]: {
    padding: '1.5rem 2rem',
  },
}));

export const SectionHeader = styled('div')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.75rem',
  paddingBottom: '0.75rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
}));

export const SectionTitle = styled('div')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
}));

export const SectionTitleCount = styled('span')(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
}));

export const ItemList = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.625rem',
});

export const ItemCard = styled(Box)(({ theme }) => ({
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 8,
  padding: '0.875rem 1rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.5rem',
  transition: 'border-color 0.15s ease, background-color 0.15s ease',
  '&:hover': {
    borderColor: theme.palette.text.disabled,
    backgroundColor: theme.palette.headerBg,
  },
}));

export const ItemHeader = styled('div')({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.75rem',
  flexWrap: 'wrap',
});

export const ItemHeaderLeft = styled('div')({
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  flexWrap: 'wrap',
  minWidth: 0,
});

export const ItemHeaderRight = styled('div')({
  display: 'flex',
  alignItems: 'center',
  gap: '0.25rem',
});

export const ItemTitle = styled('div')(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
}));

export const ItemSubtle = styled('div')(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
}));

export const ItemContent = styled('div')(({ theme }) => ({
  fontSize: '0.875rem',
  color: theme.palette.text.primary,
  whiteSpace: 'pre-wrap',
  lineHeight: 1.6,
  paddingTop: '0.25rem',
  borderTop: `1px dashed ${theme.palette.divider}`,
}));

export const ItemTitleLink = styled('button')(({ theme }) => ({
  background: 'none',
  border: 'none',
  padding: 0,
  margin: 0,
  cursor: 'pointer',
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.primary.main,
  textAlign: 'left',
  '&:hover': { textDecoration: 'underline' },
}));

export const EmptySection = styled('div')(({ theme }) => ({
  padding: '1.5rem 1rem',
  textAlign: 'center',
  color: theme.palette.text.disabled,
  fontSize: '0.875rem',
  border: `1px dashed ${theme.palette.divider}`,
  borderRadius: 8,
}));
