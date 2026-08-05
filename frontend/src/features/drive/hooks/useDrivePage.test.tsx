import type { ChangeEvent, ReactNode } from 'react';
import { act, renderHook } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { UPLOAD_FILE_SIZE_GUIDE } from '@/shared/utils/uploadFileSize';
import { useDrivePage } from './useDrivePage';

const mocks = vi.hoisted(() => ({
  upload: vi.fn(),
  uploadUnwrap: vi.fn(),
  success: vi.fn(),
  error: vi.fn(),
  warning: vi.fn(),
}));

vi.mock('@/features/drive/api/driveApi', () => ({
  useBrowseDriveQuery: () => ({
    data: { breadcrumb: [], folders: [], files: [] },
    isLoading: false,
    isFetching: false,
    isError: false,
  }),
  useDeleteDriveFileMutation: () => [vi.fn(), { isLoading: false }],
  useDeleteDriveFolderMutation: () => [vi.fn(), { isLoading: false }],
  useDriveFileDownload: () => vi.fn(),
  useUploadDriveFileMutation: () => [mocks.upload],
}));

vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ canRead: true, canWrite: true }),
}));

vi.mock('@/shared/hooks/useApiSubmit', () => ({
  useApiSubmit: () => vi.fn(),
}));

vi.mock('@/shared/ui/feedback/snackbar', () => ({
  useSnackbar: () => ({
    success: mocks.success,
    error: mocks.error,
    warning: mocks.warning,
  }),
}));

function Wrapper({ children }: { children: ReactNode }) {
  return <MemoryRouter>{children}</MemoryRouter>;
}

describe('useDrivePage upload adapter', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.uploadUnwrap.mockResolvedValue(13);
    mocks.upload.mockReturnValue({ unwrap: mocks.uploadUnwrap });
  });

  it('업로드 상한을 미리 안내하고 선택 파일을 FormData로 전달한다', async () => {
    const { result } = renderHook(() => useDrivePage(), { wrapper: Wrapper });
    expect(result.current.headerActions[1]?.label)
      .toBe(`파일 업로드 · ${UPLOAD_FILE_SIZE_GUIDE}`);

    const file = new File(['drive'], '합성-운영안내.txt', { type: 'text/plain' });
    const input = document.createElement('input');
    input.type = 'file';
    Object.defineProperty(input, 'files', { configurable: true, value: [file] });

    await act(async () => {
      await result.current.onUploadFileSelected({
        target: input,
      } as ChangeEvent<HTMLInputElement>);
    });

    expect(mocks.upload).toHaveBeenCalledOnce();
    const request = mocks.upload.mock.calls[0][0] as {
      folderId: number | null;
      form: FormData;
    };
    expect(request.folderId).toBeNull();
    expect(request.form.get('file')).toBe(file);
    expect(mocks.uploadUnwrap).toHaveBeenCalledOnce();
    expect(mocks.success).toHaveBeenCalledWith('파일이 업로드되었습니다.');
    expect(input.value).toBe('');
  });
});
