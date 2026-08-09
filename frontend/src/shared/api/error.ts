import type { ApiError } from '@/shared/types/api';

export const DEMO_RESET_IN_PROGRESS_MESSAGE =
  '데모 데이터를 초기화하거나 검증하고 있습니다. 잠시 후 다시 시도해 주세요.';

const ERROR_CODE_MESSAGES: Record<string, string> = {
  DEMO_RESET_IN_PROGRESS: DEMO_RESET_IN_PROGRESS_MESSAGE,
  DEMO_UPLOAD_DISABLED: '데모에서는 파일 업로드가 비활성화되어 있습니다.',
  DEMO_PROTECTED_RESOURCE: '데모 복구에 필요한 보호 항목은 변경할 수 없습니다.',
  DEMO_RATE_LIMIT_EXCEEDED: '데모 요청 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.',
};

export function isApiError(err: unknown): err is ApiError {
  return (
    typeof err === 'object' &&
    err !== null &&
    'message' in err &&
    typeof (err as { message: unknown }).message === 'string'
  );
}

export function getErrorMessage(err: unknown): string | undefined;
export function getErrorMessage(err: unknown, fallback: string): string;
export function getErrorMessage(err: unknown, fallback?: string): string | undefined {
  if (isApiError(err)) {
    if (err.code && ERROR_CODE_MESSAGES[err.code]) return ERROR_CODE_MESSAGES[err.code];
    return err.message;
  }
  return fallback;
}
