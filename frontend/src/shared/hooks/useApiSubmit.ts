import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getErrorMessage } from '@/shared/api/error';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';

interface SubmitOptions<T> {
  success?: string;
  error?: string;
  navigateTo?: string;
  /** 성공 시 추가 사이드이펙트 — modal close 등. */
  onSuccess?: (result: T) => void;
}

/** RTK Query mutation 의 unwrap promise 를 받아 snackbar success/error + 선택적 navigate / onSuccess. 실패 시 null. */
export function useApiSubmit() {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  return useCallback(
    async <T>(
      promise: { unwrap: () => Promise<T> },
      opts: SubmitOptions<T> = {},
    ): Promise<T | null> => {
      try {
        const result = await promise.unwrap();
        if (opts.success) snackbar.success(opts.success);
        if (opts.navigateTo) navigate(opts.navigateTo);
        opts.onSuccess?.(result);
        return result;
      } catch (err) {
        snackbar.error(getErrorMessage(err, opts.error ?? '저장 중 오류가 발생했습니다.'));
        return null;
      }
    },
    [navigate, snackbar],
  );
}
