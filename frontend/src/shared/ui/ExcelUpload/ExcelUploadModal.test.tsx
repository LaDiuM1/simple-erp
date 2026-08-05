import { fireEvent, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import {
  MAX_UPLOAD_FILE_SIZE_BYTES,
  UPLOAD_FILE_SIZE_GUIDE,
} from '@/shared/utils/uploadFileSize';
import ExcelUploadModal, { type ExcelUploadResult } from './ExcelUploadModal';

const mocks = vi.hoisted(() => ({
  error: vi.fn(),
  success: vi.fn(),
}));

const SUCCESS_RESULT: ExcelUploadResult = {
  totalRows: 1,
  successRows: 1,
  failedRows: 0,
  errors: [],
};

function createUploadMock(unwrap: () => Promise<ExcelUploadResult>) {
  const upload = vi.fn<(
    form: FormData
  ) => { unwrap: () => Promise<ExcelUploadResult> }>();
  upload.mockReturnValue({ unwrap });
  return upload;
}

vi.mock('@/shared/ui/feedback/snackbar', () => ({
  useSnackbar: () => ({
    error: mocks.error,
    success: mocks.success,
  }),
}));

describe('ExcelUploadModal', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('공통 파일 상한을 안내하고 초과 파일은 mutation 전에 거부한다', () => {
    const upload = createUploadMock(vi.fn<() => Promise<ExcelUploadResult>>());
    renderWithTheme(
      <ExcelUploadModal
        open
        onClose={vi.fn()}
        upload={upload}
      />,
    );

    expect(screen.getByText(`.xlsx 형식 · ${UPLOAD_FILE_SIZE_GUIDE}`)).toBeInTheDocument();

    const input = document.querySelector<HTMLInputElement>('input[type="file"]');
    expect(input).not.toBeNull();
    fireEvent.change(input!, {
      target: {
        files: [{
          name: 'oversized.xlsx',
          size: MAX_UPLOAD_FILE_SIZE_BYTES + 1,
        }],
      },
    });

    expect(mocks.error).toHaveBeenCalledWith(
      `oversized.xlsx — ${UPLOAD_FILE_SIZE_GUIDE}까지 업로드할 수 있습니다.`,
    );
    expect(upload).not.toHaveBeenCalled();
  });

  it('선택한 파일을 FormData로 전달하고 성공 결과를 화면에 반영한다', async () => {
    const unwrap = vi.fn<() => Promise<ExcelUploadResult>>().mockResolvedValue(SUCCESS_RESULT);
    const upload = createUploadMock(unwrap);
    renderWithTheme(
      <ExcelUploadModal
        open
        onClose={vi.fn()}
        upload={upload}
      />,
    );

    const file = new File(['row'], 'customers.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const input = document.querySelector<HTMLInputElement>('input[type="file"]');
    expect(input).not.toBeNull();
    fireEvent.change(input!, { target: { files: [file] } });

    await waitFor(() => expect(upload).toHaveBeenCalledOnce());
    const form = upload.mock.calls[0][0];
    expect(form.get('file')).toBe(file);
    expect(unwrap).toHaveBeenCalledOnce();
    expect(await screen.findByText('업로드 완료')).toBeInTheDocument();
    expect(mocks.success).toHaveBeenCalledWith('1건이 업로드되었습니다.');
  });

  it('업로드 중에는 dropzone을 비활성 상태로 알리고 추가 drop을 무시한다', () => {
    const upload = createUploadMock(vi.fn<() => Promise<ExcelUploadResult>>());
    renderWithTheme(
      <ExcelUploadModal
        open
        onClose={vi.fn()}
        upload={upload}
        isUploading
      />,
    );

    const dropzone = screen.getByRole('button', { name: /업로드 중/ });
    expect(dropzone).toHaveAttribute('aria-busy', 'true');
    expect(dropzone).toHaveAttribute('aria-disabled', 'true');
    expect(document.querySelector<HTMLInputElement>('input[type="file"]')).toBeDisabled();

    fireEvent.drop(dropzone, {
      dataTransfer: { files: [new File(['row'], 'customers.xlsx')] },
    });
    expect(upload).not.toHaveBeenCalled();
  });

  it('외부 loading 반영 전 같은 tick에 연속 drop되어도 하나만 업로드한다', async () => {
    let resolveUpload: (value: ExcelUploadResult) => void = () => undefined;
    const unwrap = vi.fn(() => new Promise<ExcelUploadResult>((resolve) => {
      resolveUpload = resolve;
    }));
    const upload = createUploadMock(unwrap);
    renderWithTheme(
      <ExcelUploadModal
        open
        onClose={vi.fn()}
        upload={upload}
      />,
    );

    const dropzone = screen.getByRole('button', { name: /파일을 끌어다 놓거나/ });
    const dataTransfer = { files: [new File(['row'], 'customers.xlsx')] };
    fireEvent.drop(dropzone, { dataTransfer });
    fireEvent.drop(dropzone, { dataTransfer });

    expect(upload).toHaveBeenCalledOnce();
    resolveUpload(SUCCESS_RESULT);
    expect(await screen.findByText('업로드 완료')).toBeInTheDocument();
  });
});
