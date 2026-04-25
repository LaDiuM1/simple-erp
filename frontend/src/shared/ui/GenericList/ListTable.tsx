import { useState, type ReactNode } from 'react';
import CircularProgress from '@mui/material/CircularProgress';
import IconButton from '@mui/material/IconButton';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Tooltip from '@mui/material/Tooltip';
import DeleteIcon from '@mui/icons-material/DeleteOutline';
import EditIcon from '@mui/icons-material/EditOutlined';
import InboxIcon from '@mui/icons-material/InboxOutlined';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import Muted from '@/shared/ui/atoms/Muted';
import { usePermission } from '@/shared/hooks/usePermission';
import {
  BodyCell,
  BodyRow,
  EmptyStateContainer,
  EmptyStateText,
  HeaderCell,
  LoadingOverlayBox,
  MobileCardItem,
  MobileDetailLabel,
  MobileDetailRow,
  MobileDetailValue,
  MobilePrimaryRow,
  RowActionsCell,
  StyledSortLabel,
  StyledTableContainer,
  TableScrollArea,
  TableWrapper,
} from './ListTable.styles';
import type { ColumnConfig, DeleteConfirmMessages, SortState } from './types';

const DEFAULT_DELETE_CONFIRM: DeleteConfirmMessages = {
  title: '항목 삭제',
  message: '이 항목을 삭제하시겠습니까?',
};

const DEFAULT_EMPTY_MESSAGE = '등록된 항목이 없습니다.';

interface Props<TRow> {
  menuCode: string;
  columns: ColumnConfig<TRow>[];
  rows: TRow[];
  rowKey: (row: TRow) => string | number;
  page: number;
  pageSize: number;
  sort: SortState;
  onSortChange: (sort: SortState) => void;
  isLoading?: boolean;
  emptyMessage?: string;
  onEdit?: (row: TRow) => void;
  onDelete?: (row: TRow) => Promise<void> | void;
  deleteConfirm?: DeleteConfirmMessages;
}

/** 데이터 전체 순번 = 현재 페이지 인덱스 × size + 행 인덱스 + 1. */
const NO_COL_WIDTH = 64;

/**
 * 표 영역. 데스크탑은 Table, 모바일은 카드 리스트.
 * 수정/삭제 행 액션 + 삭제 확인 모달까지 내재.
 */
export default function ListTable<TRow>({
  menuCode,
  columns,
  rows,
  rowKey,
  page,
  pageSize,
  sort,
  onSortChange,
  isLoading = false,
  emptyMessage = DEFAULT_EMPTY_MESSAGE,
  onEdit,
  onDelete,
  deleteConfirm,
}: Props<TRow>) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { canWrite } = usePermission(menuCode);

  const [deletingRow, setDeletingRow] = useState<TRow | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const handleSortClick = (col: ColumnConfig<TRow>) => {
    if (!col.sortable) return;
    // 같은 컬럼 재클릭: 방향 토글. 다른 컬럼 첫 클릭: 해당 컬럼의 기본 방향 사용.
    const nextDirection =
      sort.key === col.key
        ? sort.direction === 'asc'
          ? 'desc'
          : 'asc'
        : col.sortDirection;
    onSortChange({ key: col.key, direction: nextDirection });
  };

  const requestDelete = onDelete ? (row: TRow) => setDeletingRow(row) : undefined;

  const handleConfirmDelete = async () => {
    if (!deletingRow || !onDelete) return;
    setIsDeleting(true);
    try {
      await onDelete(deletingRow);
    } catch {
      // GenericList 가 이미 스낵바로 에러를 노출한다. 모달은 닫고 재시도는 사용자 재클릭으로 유도.
    } finally {
      setIsDeleting(false);
      setDeletingRow(null);
    }
  };

  const rowActions = canWrite && (onEdit || requestDelete)
    ? (row: TRow) => (
        <Stack direction="row" spacing={0.25} sx={{ justifyContent: 'flex-end' }}>
          {onEdit && (
            <Tooltip title="수정" arrow>
              <IconButton
                size="small"
                aria-label="수정"
                onClick={() => onEdit(row)}
                sx={{ '&:hover': { color: 'primary.main' } }}
              >
                <EditIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
          {requestDelete && (
            <Tooltip title="삭제" arrow>
              <IconButton
                size="small"
                aria-label="삭제"
                onClick={() => requestDelete(row)}
                sx={{ '&:hover': { color: 'error.main' } }}
              >
                <DeleteIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          )}
        </Stack>
      )
    : undefined;

  const confirm = { ...DEFAULT_DELETE_CONFIRM, ...deleteConfirm };

  return (
    <TableWrapper>
      <TableScrollArea>
        {isMobile ? (
          <MobileCards
            columns={columns}
            rows={rows}
            rowKey={rowKey}
            rowActions={rowActions}
            emptyMessage={emptyMessage}
          />
        ) : (
          <DesktopTable
            columns={columns}
            rows={rows}
            rowKey={rowKey}
            rowActions={rowActions}
            page={page}
            pageSize={pageSize}
            sort={sort}
            onSortClick={handleSortClick}
            emptyMessage={emptyMessage}
          />
        )}
      </TableScrollArea>

      {isLoading && (
        <LoadingOverlayBox>
          <CircularProgress size={36} thickness={4} />
        </LoadingOverlayBox>
      )}

      {onDelete && (
        <ConfirmModal
          isOpen={deletingRow !== null}
          title={confirm.title}
          message={confirm.message}
          confirmLabel={isDeleting ? '삭제 중...' : '삭제'}
          danger
          onConfirm={handleConfirmDelete}
          onCancel={() => setDeletingRow(null)}
        />
      )}
    </TableWrapper>
  );
}

/* --------------------------------------------------------------------------
 * Desktop
 * ------------------------------------------------------------------------ */

interface DesktopProps<TRow> {
  columns: ColumnConfig<TRow>[];
  rows: TRow[];
  rowKey: (row: TRow) => string | number;
  rowActions?: (row: TRow) => ReactNode;
  page: number;
  pageSize: number;
  sort: SortState;
  onSortClick: (col: ColumnConfig<TRow>) => void;
  emptyMessage: string;
}

function DesktopTable<TRow>({
  columns, rows, rowKey, rowActions, page, pageSize, sort, onSortClick, emptyMessage,
}: DesktopProps<TRow>) {
  return (
    <StyledTableContainer>
      <Table size="small" sx={{ minWidth: 720 }}>
        <TableHead>
          <TableRow>
            <HeaderCell align="center" sx={{ width: NO_COL_WIDTH }}>No</HeaderCell>
            {columns.map((col) => (
              <HeaderCell
                key={col.key}
                align={col.align ?? 'left'}
                sortDirection={sort.key === col.key ? sort.direction : false}
                sx={{ width: col.width }}
              >
                {col.sortable ? (
                  <StyledSortLabel
                    active={sort.key === col.key}
                    direction={sort.key === col.key ? sort.direction : col.sortDirection}
                    onClick={() => onSortClick(col)}
                  >
                    {col.label}
                  </StyledSortLabel>
                ) : col.label}
              </HeaderCell>
            ))}
            {rowActions && <HeaderCell align="right" sx={{ width: 100 }} />}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.length === 0 ? (
            <TableRow>
              <BodyCell
                colSpan={columns.length + 1 + (rowActions ? 1 : 0)}
                sx={{ p: 0 }}
              >
                <EmptyState message={emptyMessage} />
              </BodyCell>
            </TableRow>
          ) : (
            rows.map((row, idx) => (
              <BodyRow key={rowKey(row)}>
                <BodyCell align="center" sx={{ color: 'text.secondary', width: NO_COL_WIDTH }}>
                  {page * pageSize + idx + 1}
                </BodyCell>
                {columns.map((col) => (
                  <BodyCell key={col.key} align={col.align ?? 'left'}>
                    {renderCell(col, row)}
                  </BodyCell>
                ))}
                {rowActions && (
                  <RowActionsCell align="right" className="row-actions">
                    {rowActions(row)}
                  </RowActionsCell>
                )}
              </BodyRow>
            ))
          )}
        </TableBody>
      </Table>
    </StyledTableContainer>
  );
}

/* --------------------------------------------------------------------------
 * Mobile
 * ------------------------------------------------------------------------ */

interface MobileProps<TRow> {
  columns: ColumnConfig<TRow>[];
  rows: TRow[];
  rowKey: (row: TRow) => string | number;
  rowActions?: (row: TRow) => ReactNode;
  emptyMessage: string;
}

function MobileCards<TRow>({ columns, rows, rowKey, rowActions, emptyMessage }: MobileProps<TRow>) {
  const primary = columns.find((c) => c.mobilePrimary) ?? columns[0];
  const details = columns.filter((c) => c.key !== primary?.key && !c.hideOnMobile);

  if (rows.length === 0) {
    return <EmptyState message={emptyMessage} />;
  }

  return (
    <>
      {rows.map((row) => (
        <MobileCardItem key={rowKey(row)}>
          <MobilePrimaryRow>
            <div style={{ minWidth: 0, flex: 1 }}>{renderCell(primary, row)}</div>
            {rowActions && <div style={{ flexShrink: 0 }}>{rowActions(row)}</div>}
          </MobilePrimaryRow>
          {details.length > 0 && (
            <Stack spacing={0.625} sx={{ mt: '0.875rem' }}>
              {details.map((col) => (
                <MobileDetailRow key={col.key}>
                  <MobileDetailLabel>{col.label}</MobileDetailLabel>
                  <MobileDetailValue>{renderCell(col, row)}</MobileDetailValue>
                </MobileDetailRow>
              ))}
            </Stack>
          )}
        </MobileCardItem>
      ))}
    </>
  );
}

/* --------------------------------------------------------------------------
 * Helpers
 * ------------------------------------------------------------------------ */

function EmptyState({ message }: { message: string }) {
  return (
    <EmptyStateContainer>
      <InboxIcon sx={{ fontSize: 44, color: 'text.disabled' }} />
      <EmptyStateText>{message}</EmptyStateText>
    </EmptyStateContainer>
  );
}

function renderCell<TRow>(col: ColumnConfig<TRow>, row: TRow): ReactNode {
  if (col.render) return col.render(row);
  const value = (row as Record<string, unknown>)[col.key];
  if (value == null) return <Muted />;
  return value as ReactNode;
}
