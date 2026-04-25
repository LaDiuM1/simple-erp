import { styled, type Theme } from '@mui/material/styles';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import type { SnackbarSeverity } from './snackbarSlice';

const severityColor = (theme: Theme, severity: SnackbarSeverity) => {
  switch (severity) {
    case 'success':
      return theme.palette.success.main;
    case 'error':
      return theme.palette.error.main;
    case 'warning':
      return theme.palette.warning.main;
    case 'info':
    default:
      return theme.palette.primary.main;
  }
};

/** 흰 카드 + 얕은 그림자 + 12px radius. severity 색은 아이콘으로만 전달. */
export const SnackbarCard = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'flex-start',
  gap: '0.75rem',
  minWidth: 320,
  maxWidth: 480,
  padding: '0.875rem 0.75rem 0.875rem 1rem',
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 12,
  boxShadow: theme.shadows[4],
}));

export const SnackbarIcon = styled('span', {
  shouldForwardProp: (prop) => prop !== 'severity',
})<{ severity: SnackbarSeverity }>(({ theme, severity }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  flexShrink: 0,
  lineHeight: 0,
  marginTop: '1px',
  color: severityColor(theme, severity),
}));

export const SnackbarMessage = styled(Typography)(({ theme }) => ({
  flex: 1,
  fontSize: '0.875rem',
  fontWeight: 500,
  color: theme.palette.text.primary,
  lineHeight: 1.5,
  wordBreak: 'break-word',
}));

export const SnackbarCloseButton = styled(IconButton)(({ theme }) => ({
  padding: '0.125rem',
  marginTop: '-1px',
  color: theme.palette.text.disabled,
  '&:hover': {
    color: theme.palette.text.secondary,
    backgroundColor: 'transparent',
  },
}));
