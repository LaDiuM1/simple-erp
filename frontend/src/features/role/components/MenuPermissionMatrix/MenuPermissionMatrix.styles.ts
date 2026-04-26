import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

export const MatrixRoot = styled(Box)(({ theme }) => ({
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 4,
  overflow: 'hidden',
}));

export const MatrixHeader = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr 96px 96px',
  alignItems: 'center',
  padding: '0.625rem 0.875rem',
  backgroundColor: theme.palette.background.default,
  borderBottom: `1px solid ${theme.palette.divider}`,
  fontSize: '0.75rem',
  fontWeight: 600,
  color: theme.palette.text.secondary,
  letterSpacing: '0.02em',
}));

export const MatrixRow = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'readOnly',
})<{ readOnly: boolean }>(({ theme, readOnly }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr 96px 96px',
  alignItems: 'center',
  padding: '0.5rem 0.875rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  '&:last-child': { borderBottom: 'none' },
  backgroundColor: readOnly ? theme.palette.background.default : 'transparent',
  '&:hover': {
    backgroundColor: readOnly ? theme.palette.background.default : theme.palette.action.hover,
  },
}));

export const MenuLabel = styled('span')(({ theme }) => ({
  fontSize: '0.875rem',
  color: theme.palette.text.primary,
}));

export const ColumnCenter = styled(Box)({
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
});

export const HeaderColumnCenter = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  gap: '0.125rem',
});

export const HeaderToggleAll = styled('button')(({ theme }) => ({
  background: 'transparent',
  border: 'none',
  padding: 0,
  cursor: 'pointer',
  fontSize: '0.6875rem',
  fontWeight: 500,
  color: theme.palette.primary.main,
  '&:hover': { textDecoration: 'underline' },
  '&:disabled': { color: theme.palette.text.disabled, cursor: 'default', textDecoration: 'none' },
}));

export const Banner = styled(Box)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  padding: '0.5rem 0.75rem',
  backgroundColor: theme.palette.primarySubtle,
  borderLeft: `3px solid ${theme.palette.primary.main}`,
  borderRadius: 4,
  marginBottom: '0.625rem',
}));
