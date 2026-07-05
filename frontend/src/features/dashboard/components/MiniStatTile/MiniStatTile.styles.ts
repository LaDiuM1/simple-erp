import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const MiniRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 10,
  padding: '0.75rem 1.125rem',
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'center',
  gap: '0.1875rem',
  minWidth: 0,
  flex: 1,
}));

export const MiniLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.71875rem',
  fontWeight: 600,
  color: theme.palette.text.secondary,
}));

export const MiniValueRow = styled(Box)({
  display: 'flex',
  alignItems: 'baseline',
  gap: '0.3125rem',
});

export const MiniValue = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'accent',
})<{ accent?: boolean }>(({ theme, accent }) => ({
  fontSize: '1.1875rem',
  fontWeight: 760,
  letterSpacing: '-0.03em',
  lineHeight: 1.05,
  fontVariantNumeric: 'tabular-nums',
  color: accent ? theme.palette.primary.main : theme.palette.text.primary,
}));

export const MiniUnit = styled(Typography)(({ theme }) => ({
  fontSize: '0.71875rem',
  color: theme.palette.text.disabled,
}));
