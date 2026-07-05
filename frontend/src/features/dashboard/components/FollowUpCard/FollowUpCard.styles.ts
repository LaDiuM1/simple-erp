import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const CardRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 10,
  padding: '1rem 0 0.375rem',
  display: 'flex',
  flexDirection: 'column',
  minWidth: 0,
  overflow: 'hidden',
}));

export const CardHead = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.5rem',
  padding: '0 1.125rem',
  marginBottom: '0.375rem',
});

export const CardTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  fontWeight: 700,
  letterSpacing: '0.1em',
  textTransform: 'uppercase',
  color: theme.palette.text.secondary,
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.4375rem',
}));

export const CountBadge = styled('span')(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  minWidth: 17,
  height: 17,
  padding: '0 0.3125rem',
  fontSize: '0.65625rem',
  fontWeight: 700,
  borderRadius: 999,
  backgroundColor: theme.palette.statusPending,
  color: '#ffffff',
  letterSpacing: 0,
}));

export const MoreLink = styled('button')(({ theme }) => ({
  appearance: 'none',
  background: 'none',
  border: 'none',
  cursor: 'pointer',
  padding: 0,
  fontFamily: 'inherit',
  fontSize: '0.71875rem',
  fontWeight: 600,
  color: theme.palette.primary.main,
  '&:hover': { textDecoration: 'underline' },
}));

export const FollowRow = styled('button')(({ theme }) => ({
  appearance: 'none',
  background: 'none',
  cursor: 'pointer',
  fontFamily: 'inherit',
  textAlign: 'left',
  width: '100%',
  display: 'flex',
  alignItems: 'center',
  gap: '0.75rem',
  padding: '0.5625rem 1.125rem',
  border: 'none',
  borderTop: `1px solid ${theme.palette.divider}`,
  transition: 'background-color 0.12s',
  minWidth: 0,
  '&:hover': { backgroundColor: theme.palette.headerBg },
}));

export const FollowBody = styled(Box)({
  flex: 1,
  minWidth: 0,
});

export const FollowName = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 640,
  color: theme.palette.text.primary,
  letterSpacing: '-0.012em',
  whiteSpace: 'nowrap',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
}));

export const FollowMeta = styled(Typography)(({ theme }) => ({
  fontSize: '0.71875rem',
  color: theme.palette.text.disabled,
  whiteSpace: 'nowrap',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
}));

export const ElapsedText = styled(Typography)(({ theme }) => ({
  flexShrink: 0,
  fontSize: '0.6875rem',
  fontWeight: 650,
  color: theme.palette.statusPending,
  fontVariantNumeric: 'tabular-nums',
}));

export const EmptyText = styled(Box)(({ theme }) => ({
  padding: '1.75rem 1.125rem',
  textAlign: 'center',
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
}));
