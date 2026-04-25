import { alpha, styled, type Theme } from '@mui/material/styles';
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

/**
 * 반투명 severity 컬러 카드. 상단 중앙에 떠서 ERP 가 요구하는 명확한 결과 피드백 제공.
 * 흰 글자 + backdrop blur 로 카드와 본문 구분.
 */
export const SnackbarCard = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'severity',
})<{ severity: SnackbarSeverity }>(({ theme, severity }) => {
  const tint = severityColor(theme, severity);
  return {
    display: 'flex',
    alignItems: 'flex-start',
    gap: '0.75rem',
    minWidth: 360,
    maxWidth: 560,
    padding: '0.875rem 0.875rem 0.875rem 1rem',
    backgroundColor: alpha(tint, 0.7),
    border: `1px solid ${alpha(tint, 0.55)}`,
    borderRadius: 12,
    boxShadow: '0 12px 28px -10px rgba(0, 0, 0, 0.25), 0 6px 12px -6px rgba(0, 0, 0, 0.15)',
    color: theme.palette.common.white,
    textShadow: '0 1px 2px rgba(0, 0, 0, 0.35)',
  };
});

export const SnackbarIcon = styled('span')({
  display: 'inline-flex',
  alignItems: 'center',
  flexShrink: 0,
  lineHeight: 0,
  marginTop: '1px',
  color: 'inherit',
});

export const SnackbarMessage = styled(Typography)({
  flex: 1,
  fontSize: '0.875rem',
  fontWeight: 600,
  color: 'inherit',
  lineHeight: 1.5,
  wordBreak: 'break-word',
  letterSpacing: '-0.005em',
});

export const SnackbarCloseButton = styled(IconButton)({
  padding: '0.125rem',
  marginTop: '-1px',
  color: 'rgba(255, 255, 255, 0.75)',
  '&:hover': {
    color: 'rgba(255, 255, 255, 1)',
    backgroundColor: 'rgba(255, 255, 255, 0.12)',
  },
});
