import { useCallback } from 'react';
import axiosInstance from '@/shared/api/axiosInstance';
import { extractFilename, triggerBrowserDownload } from '@/shared/api/excelDownload';
import { useAppSelector } from '@/app/hooks';

/**
 * 인증 포함 범용 파일 다운로드 훅 — binary 응답이라 RTK Query baseQuery(JSON 파싱)와 맞지 않아 axios 직접 호출.
 * `Content-Disposition` 파일명 우선, 없으면 fallbackName 사용.
 */
export function useFileDownload() {
  const token = useAppSelector((s) => s.auth.accessToken);

  return useCallback(
    async (url: string, fallbackName: string) => {
      const response = await axiosInstance.get(url, {
        responseType: 'blob',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      const filename = extractFilename(response.headers['content-disposition']) ?? fallbackName;
      triggerBrowserDownload(response.data, filename);
    },
    [token],
  );
}
