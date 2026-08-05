import { useCallback } from 'react';
import { useBlobDownload } from '@/shared/api/useBlobDownload';

/**
 * 인증 포함 범용 파일 다운로드 훅 — binary 응답이라 RTK Query baseQuery(JSON 파싱)와 맞지 않아 axios 직접 호출.
 * `Content-Disposition` 파일명 우선, 없으면 fallbackName 사용.
 */
export function useFileDownload() {
  const download = useBlobDownload();

  return useCallback(
    (url: string, fallbackName: string) => download({ url, fallbackName }),
    [download],
  );
}
