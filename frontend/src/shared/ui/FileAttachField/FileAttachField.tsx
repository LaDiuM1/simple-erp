import { useRef, useState } from 'react';
import CloseIcon from '@mui/icons-material/Close';
import { isAxiosError } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { useAppSelector } from '@/app/hooks';
import { useSnackbar } from '@/shared/ui/feedback/snackbar/useSnackbar';
import { formatFileSize } from '@/shared/utils/formatFileSize';
import type { ApiResponse } from '@/shared/types/api';
import {
  AttachButton,
  AttachedList,
  AttachedName,
  AttachedRow,
  AttachedSize,
  FieldLabel,
  FieldRoot,
  HiddenInput,
  RemoveButton,
} from './FileAttachField.styles';

/** 첨부된 파일 메타 — 폼 상태가 보유하고 submit 시 fileId 만 추출해 전송. */
export interface AttachedFile {
  fileId: number;
  name: string;
  size: number;
}

/** BE StoredFileInfo (`POST /api/v1/files` 응답) */
interface StoredFileInfo {
  id: number;
  originalName: string;
  contentType: string;
  size: number;
}

interface Props {
  label?: string;
  value: AttachedFile[];
  /**
   * 함수형 갱신 — 업로드 완료 시점의 stale 스냅샷이 업로드 중 변경된 다른 폼 입력을
   * 덮어쓰지 않도록 prev 기반으로 반영한다.
   */
  onChange: (update: (prev: AttachedFile[]) => AttachedFile[]) => void;
  disabled?: boolean;
  /** 단일 파일만 허용 (영수증 등) — 기본 다중 */
  single?: boolean;
}

/** BE multipart 한도 (application.properties max-file-size) 미러 — 초과분은 요청 전에 차단. */
const MAX_UPLOAD_SIZE_BYTES = 30 * 1024 * 1024;

/**
 * 파일 첨부 공용 필드 — 선택 즉시 스토리지 (`POST /api/v1/files`) 업로드 후 fileId 목록을 폼에 반영.
 * 전자결재 / 경비 영수증 / 게시판 첨부가 공용 사용.
 */
export default function FileAttachField({ label = '첨부 파일', value, onChange, disabled, single }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const token = useAppSelector((s) => s.auth.accessToken);
  const snackbar = useSnackbar();

  const handleSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    const oversized = Array.from(files).find((file) => file.size > MAX_UPLOAD_SIZE_BYTES);
    if (oversized) {
      snackbar.error(`${oversized.name} — 파일당 최대 ${formatFileSize(MAX_UPLOAD_SIZE_BYTES)} 까지 업로드할 수 있습니다.`);
      if (inputRef.current) inputRef.current.value = '';
      return;
    }

    setUploading(true);
    try {
      const uploaded: AttachedFile[] = [];
      for (const file of Array.from(files)) {
        const formData = new FormData();
        formData.append('file', file);
        const response = await axiosInstance.post<ApiResponse<StoredFileInfo>>('/api/v1/files', formData, {
          headers: token ? { Authorization: `Bearer ${token}` } : {},
        });
        const info = response.data.data;
        uploaded.push({ fileId: info.id, name: info.originalName, size: info.size });
      }
      onChange((prev) => (single ? uploaded.slice(-1) : [...prev, ...uploaded]));
    } catch (err) {
      const serverMessage = isAxiosError(err)
        ? (err.response?.data as { message?: string } | undefined)?.message
        : undefined;
      snackbar.error(serverMessage ?? '파일 업로드에 실패했습니다.');
    } finally {
      setUploading(false);
      // 같은 파일 재선택 가능하도록 초기화
      if (inputRef.current) inputRef.current.value = '';
    }
  };

  const handleRemove = (fileId: number) => {
    onChange((prev) => prev.filter((f) => f.fileId !== fileId));
  };

  return (
    <FieldRoot>
      <FieldLabel>{label}</FieldLabel>
      <AttachButton
        variant="outlined"
        size="small"
        disabled={disabled || uploading}
        onClick={() => inputRef.current?.click()}
      >
        {uploading ? '업로드 중...' : '파일 선택'}
      </AttachButton>
      <HiddenInput ref={inputRef} type="file" multiple={!single} onChange={handleSelect} />
      {value.length > 0 && (
        <AttachedList>
          {value.map((file) => (
            <AttachedRow key={file.fileId}>
              <AttachedName>{file.name}</AttachedName>
              <AttachedSize>{formatFileSize(file.size)}</AttachedSize>
              {!disabled && (
                <RemoveButton size="small" onClick={() => handleRemove(file.fileId)} aria-label="첨부 제거">
                  <CloseIcon sx={{ fontSize: '0.875rem' }} />
                </RemoveButton>
              )}
            </AttachedRow>
          ))}
        </AttachedList>
      )}
    </FieldRoot>
  );
}
