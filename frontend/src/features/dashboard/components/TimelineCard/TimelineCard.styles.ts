import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

export const CardRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 10,
  padding: '1rem 0 0.5rem',
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
  marginBottom: '0.5rem',
});

export const CardTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  fontWeight: 700,
  letterSpacing: '0.1em',
  textTransform: 'uppercase',
  color: theme.palette.text.secondary,
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

export const TimelineRow = styled('button')(({ theme }) => ({
  appearance: 'none',
  background: 'none',
  cursor: 'pointer',
  fontFamily: 'inherit',
  textAlign: 'left',
  width: '100%',
  display: 'flex',
  gap: '0.6875rem',
  padding: '0.375rem 1.125rem 0',
  border: 'none',
  minWidth: 0,
  transition: 'background-color 0.12s',
  '&:hover': { backgroundColor: theme.palette.headerBg },
}));

export const TimelineRail = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  flexShrink: 0,
  width: 12,
});

export const TimelineDot = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'latest',
})<{ latest?: boolean }>(({ theme, latest }) => ({
  width: 8,
  height: 8,
  borderRadius: '50%',
  marginTop: 6,
  flexShrink: 0,
  backgroundColor: latest ? theme.palette.primary.main : theme.palette.background.paper,
  border: `1.5px solid ${latest ? theme.palette.primary.main : theme.palette.text.disabled}`,
}));

export const TimelineLine = styled(Box)(({ theme }) => ({
  width: 1,
  flex: 1,
  backgroundColor: theme.palette.divider,
  marginTop: 3,
}));

export const RowBody = styled(Box)({
  flex: 1,
  minWidth: 0,
  paddingBottom: '0.5625rem',
});

export const RowTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 640,
  color: theme.palette.text.primary,
  letterSpacing: '-0.012em',
  whiteSpace: 'nowrap',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  '& i': {
    fontStyle: 'normal',
    fontWeight: 650,
    fontSize: '0.71875rem',
    color: theme.palette.text.secondary,
    marginRight: '0.375rem',
  },
}));

export const RowMeta = styled(Typography)(({ theme }) => ({
  fontSize: '0.71875rem',
  color: theme.palette.text.disabled,
  whiteSpace: 'nowrap',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
}));

export const RowTime = styled(Typography)(({ theme }) => ({
  flexShrink: 0,
  fontSize: '0.6875rem',
  color: theme.palette.text.disabled,
  fontVariantNumeric: 'tabular-nums',
  marginTop: 2,
}));

export const EmptyText = styled(Box)(({ theme }) => ({
  padding: '1.75rem 1.125rem',
  textAlign: 'center',
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
}));
