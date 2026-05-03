import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Select from '@mui/material/Select';

const MATRIX_GRID = '1fr 88px 88px 168px';

export const MatrixRoot = styled(Box)(({ theme }) => ({
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 4,
  overflow: 'hidden',
}));

export const MatrixHeader = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: MATRIX_GRID,
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
  gridTemplateColumns: MATRIX_GRID,
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

export const ScopeSelect = styled(Select)(({ theme }) => ({
  fontSize: '0.8125rem',
  '.MuiSelect-select': {
    padding: '0.25rem 0.5rem',
    paddingRight: '1.75rem',
  },
  '&.Mui-disabled': {
    color: theme.palette.text.disabled,
    backgroundColor: 'transparent',
  },
}));

export const ScopeColumn = styled(Box)({
  display: 'flex',
  justifyContent: 'center',
  alignItems: 'center',
  paddingLeft: '0.5rem',
  paddingRight: '0.5rem',
});

