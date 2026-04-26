import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { styled } from '@mui/material/styles';

export const PaletteWrap = styled(Box)(({ theme }) => ({
  marginTop: '0.625rem',
  padding: '0.75rem 0.875rem',
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 4,
  backgroundColor: theme.palette.background.default,
}));

export const PaletteLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  fontWeight: 600,
  color: theme.palette.text.secondary,
  marginBottom: '0.5rem',
  textTransform: 'uppercase',
  letterSpacing: '0.04em',
}));

export const PaletteGroup = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  gap: '0.375rem',
  flexWrap: 'wrap',
  marginBottom: '0.375rem',
  '&:last-child': { marginBottom: 0 },
});

export const GroupLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  color: theme.palette.text.disabled,
  minWidth: '3.5rem',
}));

export const PaletteRow = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  gap: '0.375rem',
  flexWrap: 'wrap',
});

export const TokenButton = styled(Button)(({ theme }) => ({
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
  fontSize: '0.75rem',
  fontWeight: 500,
  padding: '0.25rem 0.5rem',
  minWidth: 'auto',
  height: 26,
  borderRadius: 3,
  textTransform: 'none',
  color: theme.palette.text.primary,
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  '&:hover': {
    backgroundColor: theme.palette.primarySubtle,
    borderColor: theme.palette.primary.main,
    color: theme.palette.primary.main,
  },
}));
