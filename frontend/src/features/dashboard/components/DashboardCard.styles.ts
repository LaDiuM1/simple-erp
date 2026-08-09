import { styled } from '@mui/material/styles';
import type { CSSObject, Theme } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { Link } from 'react-router-dom';

export const SectionRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 10,
  padding: '1rem 1.125rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.875rem',
  minWidth: 0,
  [theme.breakpoints.down('sm')]: { padding: '0.875rem 1rem' },
}));

export const SectionHeader = styled(Box)({
  display: 'flex',
  alignItems: 'flex-start',
  justifyContent: 'space-between',
  gap: '0.75rem',
});

export const SectionHeading = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.1875rem',
  minWidth: 0,
});

export const SectionTitle = styled('h3')(({ theme }) => ({
  margin: 0,
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.005em',
}));

export const SectionDescription = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  lineHeight: 1.4,
}));

export const SectionCount = styled('span')(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  minWidth: 22,
  height: 22,
  padding: '0 0.375rem',
  marginLeft: '0.375rem',
  borderRadius: 9999,
  backgroundColor: theme.palette.primarySubtle,
  color: theme.palette.primary.dark,
  fontSize: '0.6875rem',
  fontWeight: 700,
  verticalAlign: 'middle',
}));

export const SectionMore = styled(Link)(({ theme }) => ({
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
  textDecoration: 'none',
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.25rem',
  borderRadius: 6,
  transition: 'color 0.12s, background-color 0.12s',
  '&:hover': {
    color: theme.palette.primary.main,
    backgroundColor: theme.palette.primarySubtle,
  },
  '&:focus-visible': {
    outline: `2px solid ${theme.palette.primary.main}`,
    outlineOffset: 1,
  },
}));

export const ItemList = styled('ul')({
  listStyle: 'none',
  margin: 0,
  padding: 0,
  display: 'flex',
  flexDirection: 'column',
});

export const ItemRow = styled('li')({
  minWidth: 0,
});

const itemLayout = (theme: Theme): CSSObject => ({
  appearance: 'none',
  display: 'flex',
  alignItems: 'center',
  gap: '0.875rem',
  padding: '0.5rem',
  borderTop: `1px solid ${theme.palette.divider}`,
  borderRight: 0,
  borderBottom: 0,
  borderLeft: 0,
  background: 'none',
  color: 'inherit',
  fontFamily: 'inherit',
  textAlign: 'left',
  transition: 'background-color 0.12s',
  margin: '0 -0.5rem',
  width: 'calc(100% + 1rem)',
  borderRadius: 6,
  'li:first-of-type &': { borderTop: 'none' },
});

export const ItemContent = styled('div')(({ theme }) => itemLayout(theme));

export const ItemAction = styled(Link)(({ theme }) => ({
  ...itemLayout(theme),
  cursor: 'pointer',
  textDecoration: 'none',
  '&:hover': { backgroundColor: theme.palette.headerBg },
  '&:focus-visible': {
    outline: `2px solid ${theme.palette.primary.main}`,
    outlineOffset: -2,
  },
}));

export const ItemMain = styled('span')({
  flex: 1,
  minWidth: 0,
  display: 'flex',
  flexDirection: 'column',
  gap: '0.125rem',
});

export const ItemTopLine = styled('span')({
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  minWidth: 0,
});

export const ItemTitle = styled('span')(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.005em',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
  minWidth: 0,
}));

export const ItemMeta = styled('span')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  minWidth: 0,
  overflow: 'hidden',
}));

export const ItemTime = styled('span')(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.disabled,
  flexShrink: 0,
  fontVariantNumeric: 'tabular-nums',
}));

export const EmptyState = styled(Box)(({ theme }) => ({
  padding: '1.75rem 1rem',
  textAlign: 'center',
  fontSize: '0.875rem',
  color: theme.palette.text.secondary,
  border: `1px dashed ${theme.palette.divider}`,
  borderRadius: 10,
}));
