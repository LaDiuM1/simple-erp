import { useRef, useState, type ChangeEvent } from 'react';
import { useSearchParams } from 'react-router-dom';
import CreateNewFolderOutlinedIcon from '@mui/icons-material/CreateNewFolderOutlined';
import UploadFileOutlinedIcon from '@mui/icons-material/UploadFileOutlined';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { getErrorMessage } from '@/shared/api/error';
import { usePermission } from '@/shared/hooks/usePermission';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useToggle } from '@/shared/hooks/useToggle';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useBrowseDriveQuery,
  useDeleteDriveFileMutation,
  useDeleteDriveFolderMutation,
  useDriveFileDownload,
  useUploadDriveFileMutation,
} from '@/features/drive/api/driveApi';
import type { DriveFileItem, DriveFolderItem } from '@/features/drive/types';
import type { DriveModalProps } from '@/features/drive/components/DriveModals/DriveModals';
import { useDemo } from '@/shared/demo/DemoContext';
import {
  getUploadFileSizeError,
  UPLOAD_FILE_SIZE_GUIDE,
} from '@/shared/utils/uploadFileSize';

/**
 * 드라이브 page hook — 폴더 탐색 상태 + browse query + 업로드 / 다운로드 / 삭제 handler + 모달 state.
 * 현재 폴더는 URL `?folderId=` 로 유지 (뒤로가기 / 딥링크 자연 동작). 잘못된 파라미터는 루트로 간주.
 *
 * Hook 은 JSX 반환하지 않음 (CLAUDE.md). modal element 는 DriveModals 컴포넌트가 명시 렌더.
 */
export function useDrivePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const parsedFolderId = Number(searchParams.get('folderId'));
  const currentFolderId =
    Number.isInteger(parsedFolderId) && parsedFolderId > 0 ? parsedFolderId : null;

  const { canWrite } = usePermission(MENU_CODE.DRIVE);
  const demo = useDemo();
  const canMutate = canWrite && !demo.writeBlocked;
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const browseQuery = useBrowseDriveQuery(currentFolderId);
  const downloadDriveFile = useDriveFileDownload();
  const [uploadFileMut] = useUploadDriveFileMutation();
  const [deleteFolderMut, { isLoading: isDeletingFolder }] = useDeleteDriveFolderMutation();
  const [deleteFileMut, { isLoading: isDeletingFile }] = useDeleteDriveFileMutation();

  const [creatingFolder, createFolderModal] = useToggle();
  const [renamingFolder, setRenamingFolder] = useState<DriveFolderItem | null>(null);
  const [deletingFolder, setDeletingFolder] = useState<DriveFolderItem | null>(null);
  const [deletingFile, setDeletingFile] = useState<DriveFileItem | null>(null);

  /** 순차 업로드 진행 상태 — mutation isLoading 은 건 사이에 잠깐 꺼져 진행 표시로 부적합. */
  const [uploadProgress, setUploadProgress] = useState<{ current: number; total: number } | null>(
    null,
  );

  const fileInputRef = useRef<HTMLInputElement>(null);

  const onNavigateFolder = (folderId: number | null) =>
    setSearchParams(folderId === null ? {} : { folderId: String(folderId) });

  const onDownloadFile = (file: DriveFileItem) => {
    void downloadDriveFile(file.id, file.name);
  };

  /**
   * 다중 파일 순차 업로드 — 진행 안내는 헤더 버튼 라벨 (n/m 업로드 중...), 완료 시 성공 스낵바 1회.
   * 일부 실패 시 나머지는 계속 진행하고 첫 실패 메시지만 에러 스낵바로 노출.
   */
  const onFileSelected = async (e: ChangeEvent<HTMLInputElement>) => {
    const input = e.target;
    if (!canMutate || !demo.uploadEnabled) {
      snackbar.warning('데모 초기화 준비 중에는 파일을 업로드할 수 없습니다.');
      input.value = '';
      return;
    }
    const files = Array.from(input.files ?? []);
    if (files.length === 0) return;
    const sizeError = getUploadFileSizeError(files);
    if (sizeError) {
      snackbar.error(sizeError);
      input.value = '';
      return;
    }

    let uploadedCount = 0;
    let firstErrorMessage: string | null = null;
    for (const [index, file] of files.entries()) {
      setUploadProgress({ current: index + 1, total: files.length });
      const form = new FormData();
      form.append('file', file);
      try {
        await uploadFileMut({ folderId: currentFolderId, form }).unwrap();
        uploadedCount += 1;
      } catch (err) {
        if (firstErrorMessage === null) {
          firstErrorMessage = getErrorMessage(err, `${file.name} 업로드 중 오류가 발생했습니다.`);
        }
      }
    }
    setUploadProgress(null);

    if (firstErrorMessage === null) {
      snackbar.success(
        files.length === 1 ? '파일이 업로드되었습니다.' : `파일 ${uploadedCount}건이 업로드되었습니다.`,
      );
    } else {
      snackbar.error(firstErrorMessage);
    }
    // 같은 파일 재선택 가능하도록 초기화
    input.value = '';
  };

  const handleConfirmDeleteFolder = async () => {
    if (!deletingFolder) return;
    await submit(deleteFolderMut(deletingFolder.id), {
      success: '폴더가 삭제되었습니다.',
      error: '삭제 중 오류가 발생했습니다.',
      onSuccess: () => setDeletingFolder(null),
    });
  };

  const handleConfirmDeleteFile = async () => {
    if (!deletingFile) return;
    await submit(deleteFileMut(deletingFile.id), {
      success: '파일이 삭제되었습니다.',
      error: '삭제 중 오류가 발생했습니다.',
      onSuccess: () => setDeletingFile(null),
    });
  };

  const headerActions: PageHeaderAction[] = canMutate
    ? [
        {
          design: 'secondary',
          label: '새 폴더',
          icon: <CreateNewFolderOutlinedIcon />,
          onClick: createFolderModal.on,
          writeAction: true,
        },
        {
          design: 'create',
          label: uploadProgress
            ? `${uploadProgress.current}/${uploadProgress.total} 업로드 중...`
            : `파일 업로드 · ${UPLOAD_FILE_SIZE_GUIDE}`,
          icon: <UploadFileOutlinedIcon />,
          loading: uploadProgress !== null,
          disabled: !demo.uploadEnabled,
          onClick: () => fileInputRef.current?.click(),
        },
      ]
    : [];

  const modal: DriveModalProps = {
    currentFolderId,
    creatingFolder,
    renamingFolder,
    deletingFolder,
    deletingFile,
    isDeletingFolder,
    isDeletingFile,
    onCloseCreateFolder: createFolderModal.off,
    onCloseRenameFolder: () => setRenamingFolder(null),
    onCancelDeleteFolder: () => setDeletingFolder(null),
    onConfirmDeleteFolder: handleConfirmDeleteFolder,
    onCancelDeleteFile: () => setDeletingFile(null),
    onConfirmDeleteFile: handleConfirmDeleteFile,
  };

  return {
    queries: { browse: browseQuery },
    canWrite: canMutate,
    uploadEnabled: demo.uploadEnabled,
    headerActions,
    onNavigateFolder,
    onOpenFolder: (folder: DriveFolderItem) => onNavigateFolder(folder.id),
    onDownloadFile,
    onRenameFolder: (folder: DriveFolderItem) => setRenamingFolder(folder),
    onDeleteFolder: (folder: DriveFolderItem) => setDeletingFolder(folder),
    onDeleteFile: (file: DriveFileItem) => setDeletingFile(file),
    uploadInputRef: fileInputRef,
    onUploadFileSelected: onFileSelected,
    modal,
  };
}
