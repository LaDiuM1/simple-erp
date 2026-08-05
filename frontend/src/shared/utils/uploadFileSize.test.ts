import { describe, expect, it } from 'vitest';
import {
  getUploadFileSizeError,
  MAX_UPLOAD_FILE_SIZE_BYTES,
  UPLOAD_FILE_SIZE_GUIDE,
} from './uploadFileSize';

describe('uploadFileSize', () => {
  it('30MiB 경계 파일은 허용한다', () => {
    expect(getUploadFileSizeError([{
      name: 'boundary.bin',
      size: MAX_UPLOAD_FILE_SIZE_BYTES,
    }])).toBeNull();
  });

  it('30MiB를 1byte라도 초과하면 공통 안내로 거부한다', () => {
    expect(getUploadFileSizeError([{
      name: 'oversized.bin',
      size: MAX_UPLOAD_FILE_SIZE_BYTES + 1,
    }])).toBe(`oversized.bin — ${UPLOAD_FILE_SIZE_GUIDE}까지 업로드할 수 있습니다.`);
  });
});
