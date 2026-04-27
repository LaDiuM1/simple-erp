import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const SectionRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 12,
  padding: '0.875rem 1.375rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.5rem',
  [theme.breakpoints.down('sm')]: { padding: '0.75rem 1rem' },
}));

export const SectionHeader = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.5rem',
});

export const SectionTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.005em',
}));

export const SectionMore = styled('button')(({ theme }) => ({
  appearance: 'none',
  background: 'none',
  border: 'none',
  cursor: 'pointer',
  padding: '0.25rem 0.5rem',
  margin: '-0.25rem -0.5rem',
  fontFamily: 'inherit',
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.25rem',
  borderRadius: 6,
  transition: 'color 0.12s, background-color 0.12s',
  '&:hover': {
    color: theme.palette.primary.main,
    backgroundColor: theme.palette.primarySubtle,
  },
}));

export const ItemList = styled('ul')({
  listStyle: 'none',
  margin: 0,
  padding: 0,
  display: 'flex',
  flexDirection: 'column',
});

export const ItemRow = styled('li')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: '0.875rem',
  padding: '0.375rem 0.5rem',
  borderTop: `1px solid ${theme.palette.divider}`,
  cursor: 'pointer',
  transition: 'background-color 0.12s',
  margin: '0 -0.5rem',
  borderRadius: 6,
  '&:first-of-type': { borderTop: 'none' },
  '&:hover': { backgroundColor: theme.palette.headerBg },
}));

export const ItemMain = styled(Box)({
  flex: 1,
  minWidth: 0,
  display: 'flex',
  flexDirection: 'column',
  gap: '0.125rem',
});

export const ItemTopLine = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  minWidth: 0,
});

export const ItemTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.005em',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
  minWidth: 0,
}));

export const ItemMeta = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  minWidth: 0,
  overflow: 'hidden',
}));

export const ItemTime = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.disabled,
  flexShrink: 0,
  fontVariantNumeric: 'tabular-nums',
}));

export const EmptyState = styled(Box)(({ theme }) => ({
  padding: '2rem 1rem',
  textAlign: 'center',
  fontSize: '0.875rem',
  color: theme.palette.text.secondary,
  border: `1px dashed ${theme.palette.divider}`,
  borderRadius: 10,
}));
