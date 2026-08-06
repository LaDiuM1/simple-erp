import IconButton from '@mui/material/IconButton';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Tooltip from '@mui/material/Tooltip';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import Muted from '@/shared/ui/atoms/Muted';
import {
  BodyCell,
  BodyRow,
  EmptyState,
  HeaderCell,
  StyledTableContainer,
  TableScrollArea,
  TableWrapper,
} from '@/shared/ui/GenericList';
import { RowActions, formatDateTime } from '@/shared/ui/GenericTabbedTable';
import { formatFileSize } from '@/shared/utils/formatFileSize';
import type { DriveFileItem, DriveFolderItem } from '@/features/drive/types';
import { FileNameButton, FolderNameButton } from './DriveTable.styles';

const TABLE_MIN_WIDTH = 720;
const ROW_HEIGHT = 44;
const SIZE_COL_WIDTH = 100;
const UPLOADER_COL_WIDTH = 120;
const CREATED_AT_COL_WIDTH = 150;
const ACTIONS_COL_WIDTH = 96;
/** 액션 컬럼 제외 기본 컬럼 수 — 빈 상태 행의 colSpan 계산용. */
const BASE_COLUMN_COUNT = 4;

interface Props {
  folders: DriveFolderItem[];
  files: DriveFileItem[];
  canWrite: boolean;
  onOpenFolder: (folder: DriveFolderItem) => void;
  onDownloadFile: (file: DriveFileItem) => void;
  onRenameFolder: (folder: DriveFolderItem) => void;
  onDeleteFolder: (folder: DriveFolderItem) => void;
  onDeleteFile: (file: DriveFileItem) => void;
}

/**
 * 드라이브 통합 테이블 — 폴더 행 먼저 + 파일 행. GenericList styled primitives 재사용.
 * 폴더 행 클릭 = 진입, 파일 행 클릭 = 다운로드. 행 액션은 propagation 차단으로 분리.
 */
export default function DriveTable({
  folders,
  files,
  canWrite,
  onOpenFolder,
  onDownloadFile,
  onRenameFolder,
  onDeleteFolder,
  onDeleteFile,
}: Props) {
  const isEmpty = folders.length === 0 && files.length === 0;
  const columnCount = BASE_COLUMN_COUNT + (canWrite ? 1 : 0);

  return (
    <TableWrapper>
      <TableScrollArea>
        <StyledTableContainer>
          <Table size="small" sx={{ tableLayout: 'fixed', width: '100%', minWidth: TABLE_MIN_WIDTH }}>
            <colgroup>
              <col />
              <col style={{ width: SIZE_COL_WIDTH }} />
              <col style={{ width: UPLOADER_COL_WIDTH }} />
              <col style={{ width: CREATED_AT_COL_WIDTH }} />
              {canWrite && <col style={{ width: ACTIONS_COL_WIDTH }} />}
            </colgroup>
            <TableHead>
              <TableRow>
                <HeaderCell>이름</HeaderCell>
                <HeaderCell>크기</HeaderCell>
                <HeaderCell>올린 사람</HeaderCell>
                <HeaderCell>등록일</HeaderCell>
                {canWrite && <HeaderCell align="right">액션</HeaderCell>}
              </TableRow>
            </TableHead>
            <TableBody>
              {isEmpty ? (
                <TableRow>
                  <BodyCell colSpan={columnCount} sx={{ p: 0 }}>
                    <EmptyState message="등록된 폴더나 파일이 없습니다." />
                  </BodyCell>
                </TableRow>
              ) : (
                <>
                  {folders.map((folder) => (
                    <BodyRow
                      key={`folder-${folder.id}`}
                      clickable
                      onClick={() => onOpenFolder(folder)}
                      style={{ height: ROW_HEIGHT }}
                    >
                      <BodyCell>
                        <FolderNameButton
                          type="button"
                          onClick={(event) => {
                            event.stopPropagation();
                            onOpenFolder(folder);
                          }}
                        >
                          {folder.name}
                        </FolderNameButton>
                      </BodyCell>
                      <BodyCell>
                        <Muted />
                      </BodyCell>
                      <BodyCell>
                        <Muted />
                      </BodyCell>
                      <BodyCell>{formatDateTime(folder.createdAt)}</BodyCell>
                      {canWrite && (
                        <BodyCell align="right">
                          <RowActions>
                            <Tooltip title="이름 변경" arrow>
                              <IconButton
                                size="small"
                                aria-label="이름 변경"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  onRenameFolder(folder);
                                }}
                                sx={{ '&:hover': { color: 'primary.main' } }}
                              >
                                <EditOutlinedIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                            <Tooltip title="삭제" arrow>
                              <IconButton
                                size="small"
                                aria-label="삭제"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  onDeleteFolder(folder);
                                }}
                                sx={{ '&:hover': { color: 'error.main' } }}
                              >
                                <DeleteOutlineIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          </RowActions>
                        </BodyCell>
                      )}
                    </BodyRow>
                  ))}
                  {files.map((file) => (
                    <BodyRow
                      key={`file-${file.id}`}
                      clickable
                      onClick={() => onDownloadFile(file)}
                      style={{ height: ROW_HEIGHT }}
                    >
                      <BodyCell>
                        <FileNameButton
                          type="button"
                          onClick={(event) => {
                            event.stopPropagation();
                            onDownloadFile(file);
                          }}
                        >
                          {file.name}
                        </FileNameButton>
                      </BodyCell>
                      <BodyCell>{formatFileSize(file.size)}</BodyCell>
                      <BodyCell>{file.uploaderName ?? <Muted />}</BodyCell>
                      <BodyCell>{formatDateTime(file.createdAt)}</BodyCell>
                      {canWrite && (
                        <BodyCell align="right">
                          <RowActions>
                            <Tooltip title="삭제" arrow>
                              <IconButton
                                size="small"
                                aria-label="삭제"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  onDeleteFile(file);
                                }}
                                sx={{ '&:hover': { color: 'error.main' } }}
                              >
                                <DeleteOutlineIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          </RowActions>
                        </BodyCell>
                      )}
                    </BodyRow>
                  ))}
                </>
              )}
            </TableBody>
          </Table>
        </StyledTableContainer>
      </TableScrollArea>
    </TableWrapper>
  );
}
