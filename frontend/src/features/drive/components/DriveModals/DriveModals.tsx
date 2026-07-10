import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import FolderNameModal from '@/features/drive/components/FolderNameModal/FolderNameModal';
import type { DriveFileItem, DriveFolderItem } from '@/features/drive/types';

export interface DriveModalProps {
  currentFolderId: number | null;
  creatingFolder: boolean;
  renamingFolder: DriveFolderItem | null;
  deletingFolder: DriveFolderItem | null;
  deletingFile: DriveFileItem | null;
  isDeletingFolder: boolean;
  isDeletingFile: boolean;
  onCloseCreateFolder: () => void;
  onCloseRenameFolder: () => void;
  onCancelDeleteFolder: () => void;
  onConfirmDeleteFolder: () => void;
  onCancelDeleteFile: () => void;
  onConfirmDeleteFile: () => void;
}

/** 드라이브 페이지 모달 묶음 — 폴더 생성 / 이름 변경 / 폴더·파일 삭제 확인. */
export default function DriveModals({ modal }: { modal: DriveModalProps }) {
  return (
    <>
      <FolderNameModal
        open={modal.creatingFolder}
        onClose={modal.onCloseCreateFolder}
        parentId={modal.currentFolderId}
      />
      <FolderNameModal
        open={modal.renamingFolder !== null}
        onClose={modal.onCloseRenameFolder}
        parentId={modal.currentFolderId}
        folder={modal.renamingFolder ?? undefined}
      />
      <ConfirmModal
        isOpen={modal.deletingFolder !== null}
        title="폴더 삭제"
        message={`'${modal.deletingFolder?.name ?? ''}' 폴더를 삭제하시겠습니까?\n하위 폴더나 파일이 있는 폴더는 삭제할 수 없습니다.`}
        confirmLabel={modal.isDeletingFolder ? '삭제 중...' : '삭제'}
        confirmDisabled={modal.isDeletingFolder}
        danger
        onConfirm={modal.onConfirmDeleteFolder}
        onCancel={modal.onCancelDeleteFolder}
      />
      <ConfirmModal
        isOpen={modal.deletingFile !== null}
        title="파일 삭제"
        message={`'${modal.deletingFile?.name ?? ''}' 파일을 삭제하시겠습니까?`}
        confirmLabel={modal.isDeletingFile ? '삭제 중...' : '삭제'}
        confirmDisabled={modal.isDeletingFile}
        danger
        onConfirm={modal.onConfirmDeleteFile}
        onCancel={modal.onCancelDeleteFile}
      />
    </>
  );
}
