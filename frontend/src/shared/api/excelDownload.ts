/**
 * 엑셀 (.xlsx) blob 다운로드 공용 헬퍼.
 * BE 의 `Content-Disposition` 헤더에서 파일명을 추출하고, 없으면 fallback 으로 도메인별 prefix + 오늘 날짜 stamp 사용.
 */

/** Blob 을 강제 다운로드 (`<a download>` 동적 생성 → click → revoke). */
export function triggerBrowserDownload(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

/**
 * `Content-Disposition` 헤더에서 filename 파싱.
 * RFC 5987 의 `filename*=UTF-8''...` / 일반 `filename=...` / 따옴표 유무 모두 대응.
 */
export function extractFilename(contentDisposition: string | undefined): string | null {
  if (!contentDisposition) return null;
  const match = contentDisposition.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/i);
  if (!match) return null;
  try {
    return decodeURIComponent(match[1]);
  } catch {
    return match[1];
  }
}

/** YYYYMMDD 형태 — fallback 파일명 stamp 용. */
export function todayStamp(): string {
  const d = new Date();
  return `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(d.getDate()).padStart(2, '0')}`;
}
