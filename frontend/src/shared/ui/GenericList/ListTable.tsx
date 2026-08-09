import {
  useLayoutEffect,
  useRef,
  useState,
  type KeyboardEvent,
  type ReactNode,
  type RefObject,
} from 'react';
import Checkbox from '@mui/material/Checkbox';
import CircularProgress from '@mui/material/CircularProgress';
import LinearProgress from '@mui/material/LinearProgress';
import IconButton from '@mui/material/IconButton';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Tooltip from '@mui/material/Tooltip';
import DeleteIcon from '@mui/icons-material/DeleteOutline';
import EditIcon from '@mui/icons-material/EditOutlined';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import { usePermission } from '@/shared/hooks/usePermission';
import {
  BodyCell,
  BodyRow,
  HeaderCell,
  LoadingOverlayBox,
  MobileCardItem,
  MobileDetailLabel,
  MobileDetailRow,
  MobileDetailValue,
  MobilePrimaryRow,
  RowPrimaryAction,
  StyledSortLabel,
  StyledTableContainer,
  TableScrollArea,
  TableWrapper,
} from './ListTable.styles';
import EmptyState from './EmptyState';
import { renderCellContent, renderTruncatableCell } from './cellRender';
import { useFillRowHeight } from './useFillRowHeight';
import { useElementWidth } from './useElementWidth';
import { computeResponsiveColumnLayout } from './utils';
import type { CellContext, ColumnConfig, DeleteConfirmMessages, SortState } from './types';
import type { ListSelectionState } from './useListSelection';

const DEFAULT_DELETE_CONFIRM: DeleteConfirmMessages = {
  title: '항목 삭제',
  message: '{no}번 항목을 삭제하시겠습니까?',
};

const DEFAULT_EMPTY_MESSAGE = '등록된 항목이 없습니다.';

interface Props<TRow> {
  menuCode: string;
  columns: ColumnConfig<TRow>[];
  rows: TRow[];
  rowKey: (row: TRow) => number;
  page: number;
  pageSize: number;
  sort: SortState;
  onSortChange: (sort: SortState) => void;
  /** 셀 render 의 2번째 인자로 전달될 컨텍스트 (현재 필터). 매치 우선 정렬 / 강조 셀 등에 사용. */
  filters: Record<string, unknown>;
  /** 캐시 없는 최초 로딩. true 면 EmptyState 를 숨겨 빈 화면 깜빡임을 막는다. */
  isLoading?: boolean;
  /** 모든 fetch 중 상태 (재조회 포함). 로딩 오버레이를 그릴지 결정한다. */
  isFetching?: boolean;
  emptyMessage?: string;
  onEdit?: (row: TRow) => void;
  onDelete?: (row: TRow) => Promise<void> | void;
  /** 행 전체 클릭 핸들러. 행 내부 액션은 propagation 차단으로 경쟁하지 않는다. */
  onRowClick?: (row: TRow) => void;
  deleteConfirm?: DeleteConfirmMessages;
  /** 체크박스 열 노출 여부. canWrite 게이트는 본 컴포넌트가 추가로 적용. */
  checkBox?: boolean;
  selection?: ListSelectionState;
  allVisibleSelected?: boolean;
  someVisibleSelected?: boolean;
  onToggleAll?: () => void;
  interactionBlocked?: boolean;
  errorMessage?: string;
  onRetry?: () => void;
}

/** 데이터 전체 순번 = 현재 페이지 인덱스 × size + 행 인덱스 + 1. */
const NO_COL_WIDTH = 64;
const CHECKBOX_COL_WIDTH = 48;
/**
 * 데스크탑 테이블 최소 폭 — 이 폭 미만으로 좁아지면 가로 스크롤 발생.
 * 100% 줌 / 표준 노트북 폭에서는 스크롤이 안 생기되, 그보다 좁아질 때만 스크롤이 보이도록 설정.
 */
const TABLE_MIN_WIDTH = 1008;

function activateWithKeyboard(event: KeyboardEvent<HTMLElement>, activate: () => void) {
  if (event.key !== 'Enter' && event.key !== ' ') return;
  event.preventDefault();
  activate();
}

/**
 * 표 영역. 데스크탑은 Table, 모바일은 카드 리스트.
 * 데스크탑은 행 클릭 → 상세 페이지 + 체크박스 일괄 삭제 (GenericList 가 처리).
 * 모바일은 카드에 수정/삭제 아이콘 + 단건 삭제 확인 모달까지 내재.
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
  filters,
  isLoading = false,
  isFetching = false,
  emptyMessage = DEFAULT_EMPTY_MESSAGE,
  onEdit,
  onDelete,
  onRowClick,
  deleteConfirm,
  checkBox,
  selection,
  allVisibleSelected,
  someVisibleSelected,
  onToggleAll,
  interactionBlocked = false,
  errorMessage,
  onRetry,
}: Props<TRow>) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const { canWrite } = usePermission(menuCode);
  const cellCtx: CellContext = { filters };

  const scrollAreaRef = useRef<HTMLDivElement>(null);
  const tableViewportWidth = useElementWidth(scrollAreaRef);
  /** 데스크탑 행 높이 — 컨테이너 높이 / pageSize, 단 minHeight 미만이면 자연 스크롤. */
  const rowHeight = useFillRowHeight(scrollAreaRef, pageSize, { minHeight: 28 });

  /**
   * 데스크탑 헤더 높이 — 로딩 오버레이가 헤더 아래 (행 영역) 부터 덮이도록 top 오프셋으로 사용.
   * 모바일은 헤더 없음 → 0 유지 → 카드 영역 전체 덮임.
   */
  const headerRef = useRef<HTMLTableSectionElement>(null);
  const [headerOffset, setHeaderOffset] = useState(0);
  useLayoutEffect(() => {
    const el = headerRef.current;
    if (!el) {
      setHeaderOffset(0);
      return;
    }
    setHeaderOffset(el.getBoundingClientRect().height);
    const observer = new ResizeObserver((entries) => {
      for (const entry of entries) {
        setHeaderOffset(entry.contentRect.height);
      }
    });
    observer.observe(el);
    return () => observer.disconnect();
  }, [isMobile]);

  /** 권한 게이트 — 체크박스 열은 옵션 + 쓰기 권한이 모두 만족될 때만 노출. */
  const showCheckboxCol = !!checkBox && canWrite && !!selection && !isMobile;

  const [deletingTarget, setDeletingTarget] = useState<{ row: TRow; no: number } | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const handleSortClick = (col: ColumnConfig<TRow>) => {
    if (interactionBlocked || !col.sortable) return;
    // 같은 컬럼 재클릭: 방향 토글. 다른 컬럼 첫 클릭: 해당 컬럼의 기본 방향 사용.
    const nextDirection =
      sort.key === col.key
        ? sort.direction === 'asc'
          ? 'desc'
          : 'asc'
        : col.sortDirection;
    onSortChange({ key: col.key, direction: nextDirection });
  };

  const requestDelete = onDelete
    ? (row: TRow, no: number) => {
        if (!interactionBlocked) setDeletingTarget({ row, no });
      }
    : undefined;

  const handleConfirmDelete = async () => {
    if (!deletingTarget || !onDelete || interactionBlocked) return;
    setIsDeleting(true);
    try {
      await onDelete(deletingTarget.row);
    } catch {
      // GenericList 가 이미 스낵바로 에러를 노출한다. 모달은 닫고 재시도는 사용자 재클릭으로 유도.
    } finally {
      setIsDeleting(false);
      setDeletingTarget(null);
    }
  };

  /**
   * 모바일 카드 전용 행 액션 — 카드 우상단에 수정 / 삭제 아이콘.
   * 데스크탑은 행 클릭 → 상세 페이지 + 체크박스 일괄 삭제로 대체되어 행 액션이 없다.
   */
  const mobileRowActions = isMobile && canWrite && (onEdit || requestDelete)
    ? (row: TRow, idx: number) => {
        const no = page * pageSize + idx + 1;
        return (
          <Stack direction="row" spacing={0.25} sx={{ justifyContent: 'flex-end' }}>
            {onEdit && (
              <Tooltip title="수정" arrow>
                <span>
                  <IconButton
                    size="small"
                    aria-label="수정"
                    disabled={interactionBlocked}
                    onClick={(e) => {
                      e.stopPropagation();
                      onEdit(row);
                    }}
                    sx={{ '&:hover': { color: 'primary.main' } }}
                  >
                    <EditIcon fontSize="small" />
                  </IconButton>
                </span>
              </Tooltip>
            )}
            {requestDelete && (
              <Tooltip title="삭제" arrow>
                <span>
                  <IconButton
                    size="small"
                    aria-label="삭제"
                    disabled={interactionBlocked}
                    onClick={(e) => {
                      e.stopPropagation();
                      requestDelete(row, no);
                    }}
                    sx={{ '&:hover': { color: 'error.main' } }}
                  >
                    <DeleteIcon fontSize="small" />
                  </IconButton>
                </span>
              </Tooltip>
            )}
          </Stack>
        );
      }
    : undefined;

  const confirm = { ...DEFAULT_DELETE_CONFIRM, ...deleteConfirm };
  const confirmMessage = deletingTarget
    ? confirm.message.replace('{no}', String(deletingTarget.no))
    : confirm.message;

  return (
    <TableWrapper aria-busy={(isLoading || isFetching) || undefined}>
      <TableScrollArea
        ref={scrollAreaRef}
        inert={isLoading ? true : undefined}
        mobilePending={isMobile && isLoading}
      >
        {isMobile ? (
          errorMessage !== undefined ? (
            <ErrorScreen message={errorMessage} onRetry={onRetry} fullScreen={false} />
          ) : (
            <MobileCards
              columns={columns}
              rows={rows}
              rowKey={rowKey}
              rowActions={mobileRowActions}
              emptyMessage={emptyMessage}
              isLoading={isLoading}
              onRowClick={interactionBlocked ? undefined : onRowClick}
              cellCtx={cellCtx}
            />
          )
        ) : (
          <DesktopTable
            columns={columns}
            rows={rows}
            rowKey={rowKey}
            page={page}
            pageSize={pageSize}
            sort={sort}
            onSortClick={handleSortClick}
            emptyMessage={emptyMessage}
            isLoading={isLoading}
            rowHeight={rowHeight}
            headerRef={headerRef}
            showCheckboxCol={showCheckboxCol}
            selection={selection}
            allVisibleSelected={allVisibleSelected}
            someVisibleSelected={someVisibleSelected}
            onToggleAll={onToggleAll}
            onRowClick={interactionBlocked ? undefined : onRowClick}
            cellCtx={cellCtx}
            viewportWidth={tableViewportWidth}
            interactionBlocked={interactionBlocked}
            errorMessage={errorMessage}
            onRetry={onRetry}
          />
        )}
      </TableScrollArea>

      {isLoading && (
        <LoadingOverlayBox
          style={{ top: headerOffset }}
          role="status"
          aria-label="목록 불러오는 중"
        >
          <CircularProgress size={36} thickness={4} />
        </LoadingOverlayBox>
      )}

      {isFetching && <LinearProgress aria-label="목록 갱신 중" sx={{ height: 2 }} />}

      {onDelete && (
        <ConfirmModal
          isOpen={deletingTarget !== null}
          title={confirm.title}
          message={confirmMessage}
          confirmLabel={isDeleting ? '삭제 중...' : '삭제'}
          danger
          confirmDisabled={isDeleting || interactionBlocked}
          onConfirm={handleConfirmDelete}
          onCancel={() => setDeletingTarget(null)}
        />
      )}
    </TableWrapper>
  );
}

interface DesktopProps<TRow> {
  columns: ColumnConfig<TRow>[];
  rows: TRow[];
  rowKey: (row: TRow) => number;
  page: number;
  pageSize: number;
  sort: SortState;
  onSortClick: (col: ColumnConfig<TRow>) => void;
  emptyMessage: string;
  isLoading?: boolean;
  /** ListTable 가 측정한 동적 행 높이 (px). 컨테이너 높이 / pageSize, minHeight 클램프. */
  rowHeight: number;
  /** 로딩 오버레이 top 오프셋 측정용 — 헤더 ResizeObserver 가 부착된다. */
  headerRef: RefObject<HTMLTableSectionElement | null>;
  showCheckboxCol?: boolean;
  selection?: ListSelectionState;
  allVisibleSelected?: boolean;
  someVisibleSelected?: boolean;
  onToggleAll?: () => void;
  onRowClick?: (row: TRow) => void;
  cellCtx: CellContext;
  viewportWidth: number;
  interactionBlocked: boolean;
  errorMessage?: string;
  onRetry?: () => void;
}

function DesktopTable<TRow>({
  columns,
  rows,
  rowKey,
  page,
  pageSize,
  sort,
  onSortClick,
  emptyMessage,
  isLoading,
  rowHeight,
  headerRef,
  showCheckboxCol,
  selection,
  allVisibleSelected,
  someVisibleSelected,
  onToggleAll,
  onRowClick,
  cellCtx,
  viewportWidth,
  interactionBlocked,
  errorMessage,
  onRetry,
}: DesktopProps<TRow>) {
  const extraColCount = (showCheckboxCol ? 1 : 0) + 1; // checkbox + No
  const primary = columns.find((column) => column.mobilePrimary) ?? columns[0];
  const reservedWidth = NO_COL_WIDTH + (showCheckboxCol ? CHECKBOX_COL_WIDTH : 0);
  const { columnWidths, tableWidth } = computeResponsiveColumnLayout(columns, {
    viewportWidth,
    reservedWidth,
    minTableWidth: TABLE_MIN_WIDTH,
  });
  return (
    <StyledTableContainer>
      <Table size="small" sx={{ tableLayout: 'fixed', width: tableWidth, minWidth: tableWidth }}>
        <colgroup>
          {showCheckboxCol && <col style={{ width: CHECKBOX_COL_WIDTH }} />}
          <col style={{ width: NO_COL_WIDTH }} />
          {columns.map((col, idx) => (
            <col key={col.key} style={{ width: columnWidths[idx] }} />
          ))}
        </colgroup>
        <TableHead ref={headerRef}>
          <TableRow>
            {showCheckboxCol && (
              <HeaderCell align="center" padding="checkbox">
                <Checkbox
                  size="small"
                  disabled={interactionBlocked}
                  checked={!!allVisibleSelected}
                  indeterminate={!allVisibleSelected && !!someVisibleSelected}
                  onChange={onToggleAll}
                  inputProps={{ 'aria-label': '현재 페이지 전체 선택' }}
                />
              </HeaderCell>
            )}
            <HeaderCell align="center">No</HeaderCell>
            {columns.map((col) => (
              <HeaderCell
                key={col.key}
                align={col.align ?? 'left'}
                sortDirection={sort.key === col.key ? sort.direction : false}
              >
                {col.sortable ? (
                  <StyledSortLabel
                    active={sort.key === col.key}
                    direction={sort.key === col.key ? sort.direction : col.sortDirection}
                    disabled={interactionBlocked}
                    onClick={() => onSortClick(col)}
                  >
                    {col.label}
                  </StyledSortLabel>
                ) : col.label}
              </HeaderCell>
            ))}
          </TableRow>
        </TableHead>
        <TableBody>
          {errorMessage !== undefined ? (
            <TableRow>
              <BodyCell colSpan={columns.length + extraColCount} sx={{ p: 0 }}>
                <ErrorScreen message={errorMessage} onRetry={onRetry} fullScreen={false} />
              </BodyCell>
            </TableRow>
          ) : rows.length === 0 ? (
            <TableRow>
              <BodyCell
                colSpan={columns.length + extraColCount}
                sx={{ p: 0 }}
              >
                {isLoading ? null : <EmptyState message={emptyMessage} />}
              </BodyCell>
            </TableRow>
          ) : (
            rows.map((row, idx) => {
              const id = rowKey(row);
              const checked = !!selection?.isSelected(id);
              return (
                <BodyRow
                  key={id}
                  clickable={!!onRowClick}
                  onClick={onRowClick ? () => onRowClick(row) : undefined}
                  style={{ height: rowHeight }}
                >
                  {showCheckboxCol && (
                    <BodyCell
                      align="center"
                      padding="checkbox"
                      onClick={(e) => e.stopPropagation()}
                    >
                      <Checkbox
                        size="small"
                        checked={checked}
                        disabled={interactionBlocked}
                        onChange={() => {
                          if (!interactionBlocked) selection?.toggle(id);
                        }}
                        inputProps={{ 'aria-label': `${id} 행 선택` }}
                      />
                    </BodyCell>
                  )}
                  <BodyCell align="center" sx={{ color: 'text.secondary' }}>
                    {page * pageSize + idx + 1}
                  </BodyCell>
                  {columns.map((col) => (
                    <BodyCell key={col.key} align={col.align ?? 'left'}>
                      {col.key === primary?.key && onRowClick ? (
                        <RowPrimaryAction
                          role="button"
                          tabIndex={0}
                          onClick={(event) => {
                            event.stopPropagation();
                            onRowClick(row);
                          }}
                          onKeyDown={(event) =>
                            activateWithKeyboard(event, () => onRowClick(row))
                          }
                        >
                          {renderTruncatableCell(col, row, cellCtx)}
                        </RowPrimaryAction>
                      ) : (
                        renderTruncatableCell(col, row, cellCtx)
                      )}
                    </BodyCell>
                  ))}
                </BodyRow>
              );
            })
          )}
        </TableBody>
      </Table>
    </StyledTableContainer>
  );
}

interface MobileProps<TRow> {
  columns: ColumnConfig<TRow>[];
  rows: TRow[];
  rowKey: (row: TRow) => string | number;
  rowActions?: (row: TRow, idx: number) => ReactNode;
  emptyMessage: string;
  isLoading?: boolean;
  onRowClick?: (row: TRow) => void;
  cellCtx: CellContext;
}

function MobileCards<TRow>({ columns, rows, rowKey, rowActions, emptyMessage, isLoading, onRowClick, cellCtx }: MobileProps<TRow>) {
  const primary = columns.find((c) => c.mobilePrimary) ?? columns[0];
  const details = columns.filter((c) => c.key !== primary?.key && !c.hideOnMobile);

  if (rows.length === 0) {
    return isLoading ? null : <EmptyState message={emptyMessage} />;
  }

  return (
    <>
      {rows.map((row, idx) => (
        <MobileCardItem
          key={rowKey(row)}
          clickable={!!onRowClick}
          onClick={onRowClick ? () => onRowClick(row) : undefined}
        >
          <MobilePrimaryRow>
            {onRowClick ? (
              <RowPrimaryAction
                role="button"
                tabIndex={0}
                onClick={(event) => {
                  event.stopPropagation();
                  onRowClick(row);
                }}
                onKeyDown={(event) => activateWithKeyboard(event, () => onRowClick(row))}
              >
                {renderCellContent(primary, row, cellCtx)}
              </RowPrimaryAction>
            ) : (
              <div style={{ minWidth: 0, flex: 1 }}>
                {renderCellContent(primary, row, cellCtx)}
              </div>
            )}
            {rowActions && (
              <div style={{ flexShrink: 0 }} onClick={(e) => e.stopPropagation()}>
                {rowActions(row, idx)}
              </div>
            )}
          </MobilePrimaryRow>
          {details.length > 0 && (
            <Stack spacing={0.625} sx={{ mt: '0.875rem' }}>
              {details.map((col) => (
                <MobileDetailRow key={col.key}>
                  <MobileDetailLabel>{col.label}</MobileDetailLabel>
                  <MobileDetailValue>{renderCellContent(col, row, cellCtx)}</MobileDetailValue>
                </MobileDetailRow>
              ))}
            </Stack>
          )}
        </MobileCardItem>
      ))}
    </>
  );
}
