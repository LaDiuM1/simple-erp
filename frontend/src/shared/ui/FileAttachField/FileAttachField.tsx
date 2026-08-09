import { useRef, useState } from 'react';
import CloseIcon from '@mui/icons-material/Close';
import { getErrorMessage } from '@/shared/api/error';
import { useUploadStoredFileMutation } from '@/shared/api/storedFileApi';
import { useSnackbar } from '@/shared/ui/feedback/snackbar/useSnackbar';
import { formatFileSize } from '@/shared/utils/formatFileSize';
import {
  getUploadFileSizeError,
  UPLOAD_FILE_SIZE_GUIDE,
} from '@/shared/utils/uploadFileSize';
import { useDemo } from '@/shared/demo/DemoContext';
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
  UploadNotice,
} from './FileAttachField.styles';

/** 첨부된 파일 메타 — 폼 상태가 보유하고 submit 시 fileId 만 추출해 전송. */
export interface AttachedFile {
  fileId: number;
  name: string;
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

/**
 * 파일 첨부 공용 필드 — 선택 즉시 스토리지 (`POST /api/v1/files`) 업로드 후 fileId 목록을 폼에 반영.
 * 전자결재 / 경비 영수증 / 게시판 첨부가 공용 사용.
 */
export default function FileAttachField({ label = '첨부 파일', value, onChange, disabled, single }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadStoredFile] = useUploadStoredFileMutation();
  const snackbar = useSnackbar();
  const demo = useDemo();
  const uploadBlocked = disabled || uploading || !demo.uploadEnabled || demo.writeBlocked;

  const handleSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (uploadBlocked) {
      e.target.value = '';
      snackbar.warning('현재 데모에서는 파일을 업로드할 수 없습니다.');
      return;
    }
    const files = e.target.files;
    if (!files || files.length === 0) return;

    const sizeError = getUploadFileSizeError(Array.from(files));
    if (sizeError) {
      snackbar.error(sizeError);
      if (inputRef.current) inputRef.current.value = '';
      return;
    }

    setUploading(true);
    try {
      const uploaded: AttachedFile[] = [];
      for (const file of Array.from(files)) {
        const info = await uploadStoredFile(file).unwrap();
        uploaded.push({ fileId: info.id, name: info.originalName, size: info.size });
      }
      onChange((prev) => (single ? uploaded.slice(-1) : [...prev, ...uploaded]));
    } catch (err) {
      snackbar.error(getErrorMessage(err, '파일 업로드에 실패했습니다.'));
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
        disabled={uploadBlocked}
        onClick={() => inputRef.current?.click()}
      >
        {uploading ? '업로드 중...' : '파일 선택'}
      </AttachButton>
      <HiddenInput
        ref={inputRef}
        type="file"
        multiple={!single}
        disabled={uploadBlocked}
        onChange={handleSelect}
      />
      {demo.status.enabled && (
        <UploadNotice>
          {demo.writeBlocked
            ? '데모 초기화 준비 중에는 파일을 업로드할 수 없습니다.'
            : demo.uploadEnabled
              ? '합성 파일만 업로드해 주세요. 실제 개인정보나 업무 자료는 입력하지 마세요.'
              : '이 데모에서는 파일 업로드가 비활성화되어 있습니다.'}
          {' '}{UPLOAD_FILE_SIZE_GUIDE}.
        </UploadNotice>
      )}
      {value.length > 0 && (
        <AttachedList>
          {value.map((file) => (
            <AttachedRow key={file.fileId}>
              <AttachedName>{file.name}</AttachedName>
              <AttachedSize>{formatFileSize(file.size)}</AttachedSize>
              {!disabled && !demo.writeBlocked && (
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
