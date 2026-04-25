import { useState, type ReactElement } from 'react';
import Snackbar from '@mui/material/Snackbar';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import ErrorRoundedIcon from '@mui/icons-material/ErrorRounded';
import InfoRoundedIcon from '@mui/icons-material/InfoRounded';
import WarningRoundedIcon from '@mui/icons-material/WarningRounded';
import CloseRoundedIcon from '@mui/icons-material/CloseRounded';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { dismiss, type SnackbarItem, type SnackbarSeverity } from './snackbarSlice';
import {
  SnackbarCard,
  SnackbarCloseButton,
  SnackbarIcon,
  SnackbarMessage,
} from './SnackbarHost.styles';

const DEFAULT_DURATION = 4000;

const ICON_MAP: Record<SnackbarSeverity, ReactElement> = {
  success: <CheckCircleRoundedIcon sx={{ fontSize: 22 }} />,
  error: <ErrorRoundedIcon sx={{ fontSize: 22 }} />,
  info: <InfoRoundedIcon sx={{ fontSize: 22 }} />,
  warning: <WarningRoundedIcon sx={{ fontSize: 22 }} />,
};

/**
 * 전역 스낵바 호스트. App.tsx 에 단일 마운트 (라우트 바깥).
 * 큐를 한 번에 하나씩 표시하며, 슬라이드-아웃 전환이 끝난 뒤(onExited)에야
 * 다음 항목을 집어 이전 메시지의 애니메이션이 끊기지 않도록 한다.
 */
export default function SnackbarHost() {
  const queue = useAppSelector((s) => s.snackbar.queue);
  const dispatch = useAppDispatch();

  const [shown, setShown] = useState<SnackbarItem | null>(null);
  const [open, setOpen] = useState(false);
  const [lastSeenId, setLastSeenId] = useState<string | null>(null);

  // 유휴 상태(전환 아웃까지 완료)에서 새 항목이 도착하면 렌더 중 상태 조정.
  // useEffect 를 피해 cascading render 를 방지하는 공식 패턴.
  const firstItem = queue[0];
  if (!shown && firstItem && firstItem.id !== lastSeenId) {
    setLastSeenId(firstItem.id);
    setShown(firstItem);
    setOpen(true);
  }

  const handleClose = (_?: unknown, reason?: string) => {
    if (reason === 'clickaway') return;
    setOpen(false);
  };

  const handleExited = () => {
    if (shown) dispatch(dismiss(shown.id));
    setShown(null);
  };

  if (!shown) return null;

  return (
    <Snackbar
      key={shown.id}
      open={open}
      autoHideDuration={shown.duration ?? DEFAULT_DURATION}
      onClose={handleClose}
      slotProps={{ transition: { onExited: handleExited } }}
      anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
    >
      <SnackbarCard role={shown.severity === 'error' ? 'alert' : 'status'}>
        <SnackbarIcon severity={shown.severity}>{ICON_MAP[shown.severity]}</SnackbarIcon>
        <SnackbarMessage>{shown.message}</SnackbarMessage>
        <SnackbarCloseButton
          size="small"
          onClick={() => setOpen(false)}
          aria-label="닫기"
        >
          <CloseRoundedIcon sx={{ fontSize: 18 }} />
        </SnackbarCloseButton>
      </SnackbarCard>
    </Snackbar>
  );
}
