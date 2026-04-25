import { useAppDispatch } from '@/app/hooks';
import { enqueue } from './snackbarSlice';

export function useSnackbar() {
  const dispatch = useAppDispatch();
  return {
    success: (message: string, duration?: number) =>
      dispatch(enqueue({ severity: 'success', message, duration })),
    error: (message: string, duration?: number) =>
      dispatch(enqueue({ severity: 'error', message, duration })),
    info: (message: string, duration?: number) =>
      dispatch(enqueue({ severity: 'info', message, duration })),
    warning: (message: string, duration?: number) =>
      dispatch(enqueue({ severity: 'warning', message, duration })),
  };
}
