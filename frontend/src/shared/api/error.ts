import type { ApiError } from '@/shared/types/api';

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
  if (isApiError(err)) return err.message;
  return fallback;
}
