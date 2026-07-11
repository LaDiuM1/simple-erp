/** 파일 크기 표기 공용 포맷 — 첨부 필드 / 결재 첨부 탭 / 드라이브가 동일 규칙 사용. */
export function formatFileSize(bytes: number): string {
  if (bytes < 1024) return `${bytes}B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)}KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)}MB`;
}
