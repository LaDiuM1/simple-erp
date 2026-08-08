import { useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '@/shared/api/baseApi';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { enqueue } from '@/shared/ui/feedback/snackbar/snackbarSlice';
import { DemoContext } from '@/shared/demo/DemoContext';
import { useGetDemoStatusQuery } from '@/features/demo/api/demoApi';
import { DISABLED_DEMO_STATUS } from '@/shared/demo/demoContract';
import { formatCountdown } from '@/features/demo/utils/countdown';
import { deriveDemoRuntime } from '@/features/demo/utils/deriveDemoRuntime';

const STATUS_POLLING_INTERVAL = 5_000;

export default function DemoProvider({ children }: { children: ReactNode }) {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const accessToken = useAppSelector((state) => state.auth.accessToken);
  const generationRef = useRef<string | null>(null);
  const [nowMs, setNowMs] = useState(() => Date.now());
  const statusQuery = useGetDemoStatusQuery(undefined, {
    pollingInterval: STATUS_POLLING_INTERVAL,
    refetchOnFocus: true,
    refetchOnReconnect: true,
  });

  const status = statusQuery.data ?? DISABLED_DEMO_STATUS;
  const statusResolved = statusQuery.data !== undefined;
  const statusUnavailable = statusQuery.isError;
  const {
    remainingMs,
    resetSoon,
    writeLocked,
    writeBlocked,
    uploadEnabled,
    maintenance,
    failed,
  } = deriveDemoRuntime(status, statusResolved, statusUnavailable, nowMs);

  useEffect(() => {
    if (!status.enabled || !status.nextResetAt) return;
    const intervalId = window.setInterval(() => setNowMs(Date.now()), 1_000);
    return () => window.clearInterval(intervalId);
  }, [status.enabled, status.nextResetAt]);

  useEffect(() => {
    if (!accessToken) {
      generationRef.current = null;
      return;
    }
    if (!status.enabled || status.state !== 'READY' || !status.generation) return;
    if (generationRef.current === null) {
      generationRef.current = status.generation;
      return;
    }
    if (generationRef.current === status.generation) return;

    generationRef.current = status.generation;
    dispatch(api.util.resetApiState());
    navigate('/', { replace: true });
    dispatch(enqueue({
      severity: 'success',
      message: '데모 데이터가 초기 상태로 복원되었습니다.',
      duration: 8_000,
    }));
  }, [accessToken, dispatch, navigate, status.enabled, status.generation, status.state]);

  const value = useMemo(() => ({
    status,
    statusResolved,
    statusUnavailable,
    resetSoon,
    remainingMs,
    countdown: formatCountdown(remainingMs),
    writeLocked,
    writeBlocked,
    uploadEnabled,
    maintenance,
    failed,
  }), [
    failed,
    maintenance,
    remainingMs,
    resetSoon,
    status,
    statusUnavailable,
    statusResolved,
    uploadEnabled,
    writeBlocked,
    writeLocked,
  ]);

  return <DemoContext.Provider value={value}>{children}</DemoContext.Provider>;
}
