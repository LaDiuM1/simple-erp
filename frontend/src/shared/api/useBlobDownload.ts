import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { isAxiosError, type AxiosRequestConfig } from 'axios';
import { useAppDispatch, useAppSelector } from '@/app/hooks';
import { performLogout } from '@/features/auth/store/authActions';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import axiosInstance from './axiosInstance';
import { extractFilename, triggerBrowserDownload } from './excelDownload';

interface BlobDownloadOptions {
  url: string;
  fallbackName: string;
  params?: AxiosRequestConfig['params'];
  paramsSerializer?: AxiosRequestConfig['paramsSerializer'];
}

async function extractDownloadError(error: unknown): Promise<string | null> {
  if (!isAxiosError(error)) return null;
  const body = error.response?.data;
  if (body instanceof Blob) {
    try {
      const parsed = JSON.parse(await body.text()) as { message?: string };
      return parsed.message ?? null;
    } catch {
      return null;
    }
  }
  if (body && typeof body === 'object' && 'message' in body) {
    const message = (body as { message?: unknown }).message;
    return typeof message === 'string' ? message : null;
  }
  return null;
}

/**
 * JSON baseQuery를 거치지 않는 blob 다운로드의 인증·오류 처리를 한 경로로 통일한다.
 * 실패는 스낵바로 노출하고 false를 반환하므로 호출부에 처리되지 않은 Promise가 남지 않는다.
 */
export function useBlobDownload() {
  const token = useAppSelector((state) => state.auth.accessToken);
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const snackbar = useSnackbar();

  return useCallback(async ({
    url,
    fallbackName,
    params,
    paramsSerializer,
  }: BlobDownloadOptions): Promise<boolean> => {
    try {
      const response = await axiosInstance.get<Blob>(url, {
        params,
        paramsSerializer,
        responseType: 'blob',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      const filename = extractFilename(response.headers['content-disposition']) ?? fallbackName;
      triggerBrowserDownload(response.data, filename);
      return true;
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 401) {
        dispatch(performLogout());
        snackbar.warning('로그인이 만료되었습니다. 다시 로그인해 주세요.');
        navigate('/login', { replace: true });
        return false;
      }

      snackbar.error((await extractDownloadError(error)) ?? '파일 다운로드에 실패했습니다.');
      return false;
    }
  }, [dispatch, navigate, snackbar, token]);
}
