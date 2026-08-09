import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import { ListRoot, ListSurface, ListSurfaceToolbar } from '@/shared/ui/GenericList';
import DriveBreadcrumb from '@/features/drive/components/DriveBreadcrumb/DriveBreadcrumb';
import DriveTable from '@/features/drive/components/DriveTable/DriveTable';
import DriveModals from '@/features/drive/components/DriveModals/DriveModals';
import { useDrivePage } from '@/features/drive/hooks/useDrivePage';
import { HiddenUploadInput } from './DrivePage.styles';

/**
 * 전사 공유 드라이브 — 폴더 탐색 + 파일 업로드 / 다운로드. GenericList 가 부적합 (페이징·검색 부재)
 * — styled primitives 만 재사용. 현재 폴더는 URL `?folderId=` 로 유지.
 */
export default function DrivePage() {
  const {
    queries,
    canWrite,
    uploadEnabled,
    headerActions,
    onNavigateFolder,
    onOpenFolder,
    onDownloadFile,
    onRenameFolder,
    onDeleteFolder,
    onDeleteFile,
    uploadInputRef,
    onUploadFileSelected,
    modal,
  } = useDrivePage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ browse }) => (
          <ListRoot>
            <ListSurface>
              <ListSurfaceToolbar visible={browse.breadcrumb.length > 0}>
                <DriveBreadcrumb breadcrumb={browse.breadcrumb} onNavigate={onNavigateFolder} />
              </ListSurfaceToolbar>
              <DriveTable
                folders={browse.folders}
                files={browse.files}
                canWrite={canWrite}
                onOpenFolder={onOpenFolder}
                onDownloadFile={onDownloadFile}
                onRenameFolder={onRenameFolder}
                onDeleteFolder={onDeleteFolder}
                onDeleteFile={onDeleteFile}
              />
            </ListSurface>
          </ListRoot>
        )}
      </QueryGate>
      <DriveModals modal={modal} />
      <HiddenUploadInput
        ref={uploadInputRef}
        type="file"
        multiple
        disabled={!uploadEnabled}
        onChange={onUploadFileSelected}
      />
    </>
  );
}
