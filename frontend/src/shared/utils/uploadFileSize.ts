import { formatFileSize } from './formatFileSize';

export const MAX_UPLOAD_FILE_SIZE_BYTES = 30 * 1024 * 1024;
export const UPLOAD_FILE_SIZE_GUIDE =
  `파일당 최대 ${formatFileSize(MAX_UPLOAD_FILE_SIZE_BYTES)}`;

type UploadFileSize = Pick<File, 'name' | 'size'>;

/** 업로드 진입점이 서버와 같은 파일당 상한을 일관되게 사전 검증한다. */
export function getUploadFileSizeError(files: readonly UploadFileSize[]): string | null {
  const oversized = files.find((file) => file.size > MAX_UPLOAD_FILE_SIZE_BYTES);
  return oversized
    ? `${oversized.name} — ${UPLOAD_FILE_SIZE_GUIDE}까지 업로드할 수 있습니다.`
    : null;
}
