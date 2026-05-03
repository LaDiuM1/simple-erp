import { useMemo, type ReactNode } from 'react';
import Checkbox from '@mui/material/Checkbox';
import CircularProgress from '@mui/material/CircularProgress';
import Radio from '@mui/material/Radio';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import {
  BodyCell,
  BodyRow,
  EmptyState,
  HeaderCell,
  LoadingOverlayBox,
  MobileCardItem,
  MobileDetailLabel,
  MobileDetailRow,
  MobileDetailValue,
  MobilePrimaryRow,
  StyledTableContainer,
  renderCellContent,
  renderTruncatableCell,
  type ColumnConfig,
} from '@/shared/ui/GenericList';
import { computeColumnWidths } from '@/shared/ui/GenericList/utils';
import { SearchTableArea } from './CommonSearchModal.styles';

interface Props<TRow> {
  rows: TRow[];
  columns: ColumnConfig<TRow>[];
  rowKey: (row: TRow) => number;
  isLoading: boolean;
  emptyMessage: string;
  page: number;
  pageSize: number;
  /** 'select': 체크박스 / 라디오 + 클릭으로 토글. 'manage': 선택 컨트롤 없음 + rowActions 노출. */
  mode?: 'select' | 'manage';
  /**
   * select 모드 — 다중 선택 시 시각 패턴.
   *  - 'checkbox': 행마다 체크박스 / 라디오 노출.
   *  - 'tray': 체크박스 없이 행 클릭으로만 토글, 선택 항목은 모달의 SelectionTray 가 표시.
   * select 모드에서만 의미.
   */
  selectionStyle?: 'checkbox' | 'tray';
  /** select 모드 — 선택 상태 조회 / 토글. */
  isSelected?: (id: number) => boolean;
  onToggleSelect?: (row: TRow) => void;
  multiple?: boolean;
  /** manage 모드 — 행마다 우측에 노출되는 액션 (예: 삭제 버튼). */
  rowActions?: (row: TRow) => ReactNode;
}

const NO_COL_WIDTH = 56;
const SELECT_COL_WIDTH = 48;
const ACTION_COL_WIDTH = 96;

/**
 * 데스크탑 행 / 헤더 높이 (px) — 본문 영역을 정확히 `ROW_HEIGHT × pageSize + HEADER_HEIGHT` 로 고정.
 * BodyCell 의 컴팩트 padding (≈29 자연 높이) 을 GenericList 메인 페이지의 `useFillRowHeight` 패턴이
 * 단독 결정하도록 의도된 값이라, 모달에서는 BodyRow 에 명시 height 를 부여해 medium 톤 (53) 강제.
 */
const ROW_HEIGHT = 53;
const HEADER_HEIGHT = 40;

/**
 * 검색 / 관리 모달 내부 리스트 — GenericList 와 동일한 데스크탑 Table / 모바일 카드 split.
 * select 모드는 행 클릭 → 선택 토글, manage 모드는 rowActions slot 만 노출.
 */
export default function SearchTable<TRow>({
  rows,
  columns,
  rowKey,
  isLoading,
  emptyMessage,
  page,
  pageSize,
  mode = 'select',
  selectionStyle = 'checkbox',
  isSelected,
  onToggleSelect,
  multiple = false,
  rowActions,
}: Props<TRow>) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));

  const visibleColumns = useMemo(
    () => (isMobile ? columns.filter((c) => !c.hideOnMobile) : columns),
    [columns, isMobile],
  );

  // 데스크탑은 본문 영역을 정확히 pageSize 행 분으로 고정 — 페이지 전환 / 데이터 양 변화에도 흔들림 없음.
  // 모바일은 자연 stack (카드 합). 좁은 뷰포트에서 컨텐츠가 paper 를 넘으면 ModalContent 가
  // 본문 자체로 스크롤하고 filter / pagination 은 sticky 로 항상 노출 (CommonSearchModal.styles 참조).
  const areaStyle = isMobile
    ? undefined
    : { height: ROW_HEIGHT * pageSize + HEADER_HEIGHT };

  return (
    <SearchTableArea style={areaStyle}>
      {isMobile ? (
        <MobileList
          columns={visibleColumns}
          rows={rows}
          rowKey={rowKey}
          mode={mode}
          selectionStyle={selectionStyle}
          isSelected={isSelected}
          onToggleSelect={onToggleSelect}
          multiple={multiple}
          rowActions={rowActions}
          emptyMessage={emptyMessage}
          isLoading={isLoading}
        />
      ) : (
        <DesktopTable
          columns={visibleColumns}
          rows={rows}
          rowKey={rowKey}
          page={page}
          pageSize={pageSize}
          mode={mode}
          selectionStyle={selectionStyle}
          isSelected={isSelected}
          onToggleSelect={onToggleSelect}
          multiple={multiple}
          rowActions={rowActions}
          emptyMessage={emptyMessage}
          isLoading={isLoading}
        />
      )}

      {isLoading && (
        <LoadingOverlayBox>
          <CircularProgress size={32} thickness={4} />
        </LoadingOverlayBox>
      )}
    </SearchTableArea>
  );
}

interface DesktopProps<TRow> {
  columns: ColumnConfig<TRow>[];
  rows: TRow[];
  rowKey: (row: TRow) => number;
  page: number;
  pageSize: number;
  mode: 'select' | 'manage';
  selectionStyle: 'checkbox' | 'tray';
  isSelected?: (id: number) => boolean;
  onToggleSelect?: (row: TRow) => void;
  multiple: boolean;
  rowActions?: (row: TRow) => ReactNode;
  emptyMessage: string;
  isLoading: boolean;
}

function DesktopTable<TRow>({
  columns,
  rows,
  rowKey,
  page,
  pageSize,
  mode,
  selectionStyle,
  isSelected,
  onToggleSelect,
  multiple,
  rowActions,
  emptyMessage,
  isLoading,
}: DesktopProps<TRow>) {
  const ToggleControl = multiple ? Checkbox : Radio;
  const isSelectMode = mode === 'select';
  const showSelectCol = isSelectMode && selectionStyle === 'checkbox';
  const showActionsCol = mode === 'manage' && !!rowActions;
  const extraColCount = (showSelectCol ? 1 : 0) + 1 + (showActionsCol ? 1 : 0);
  const domainColWidths = computeColumnWidths(columns);

  return (
    <StyledTableContainer>
      <Table sx={{ tableLayout: 'fixed', width: '100%', minWidth: 560 }}>
        <colgroup>
          {showSelectCol && <col style={{ width: SELECT_COL_WIDTH }} />}
          <col style={{ width: NO_COL_WIDTH }} />
          {columns.map((col, idx) => (
            <col key={col.key} style={{ width: domainColWidths[idx] }} />
          ))}
          {showActionsCol && <col style={{ width: ACTION_COL_WIDTH }} />}
        </colgroup>
        <TableHead>
          <TableRow>
            {showSelectCol && (
              <HeaderCell align="center" padding="checkbox" />
            )}
            <HeaderCell align="center">No</HeaderCell>
            {columns.map((col) => (
              <HeaderCell key={col.key} align={col.align ?? 'left'}>
                {col.label}
              </HeaderCell>
            ))}
            {showActionsCol && (
              <HeaderCell align="right" />
            )}
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.length === 0 ? (
            <TableRow style={{ height: ROW_HEIGHT * pageSize }}>
              <BodyCell colSpan={columns.length + extraColCount} sx={{ p: 0 }}>
                {isLoading ? null : <EmptyState message={emptyMessage} iconSize={40} />}
              </BodyCell>
            </TableRow>
          ) : (
            rows.map((row, idx) => {
              const id = rowKey(row);
              const checked = isSelectMode ? !!isSelected?.(id) : false;
              const onClick = isSelectMode && onToggleSelect ? () => onToggleSelect(row) : undefined;
              return (
                <BodyRow
                  key={id}
                  clickable={isSelectMode}
                  selected={checked}
                  onClick={onClick}
                  style={{ height: ROW_HEIGHT }}
                >
                  {showSelectCol && (
                    <BodyCell align="center" padding="checkbox">
                      <ToggleControl
                        size="small"
                        checked={checked}
                        onChange={() => onToggleSelect?.(row)}
                        onClick={(e) => e.stopPropagation()}
                      />
                    </BodyCell>
                  )}
                  <BodyCell align="center" sx={{ color: 'text.secondary' }}>
                    {page * pageSize + idx + 1}
                  </BodyCell>
                  {columns.map((col) => (
                    <BodyCell key={col.key} align={col.align ?? 'left'}>
                      {renderTruncatableCell(col, row)}
                    </BodyCell>
                  ))}
                  {showActionsCol && (
                    <BodyCell
                      align="right"
                      onClick={(e) => e.stopPropagation()}
                    >
                      {rowActions!(row)}
                    </BodyCell>
                  )}
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
  rowKey: (row: TRow) => number;
  mode: 'select' | 'manage';
  selectionStyle: 'checkbox' | 'tray';
  isSelected?: (id: number) => boolean;
  onToggleSelect?: (row: TRow) => void;
  multiple: boolean;
  rowActions?: (row: TRow) => ReactNode;
  emptyMessage: string;
  isLoading: boolean;
}

function MobileList<TRow>({
  columns,
  rows,
  rowKey,
  mode,
  selectionStyle,
  isSelected,
  onToggleSelect,
  multiple,
  rowActions,
  emptyMessage,
  isLoading,
}: MobileProps<TRow>) {
  const ToggleControl = multiple ? Checkbox : Radio;
  const isSelectMode = mode === 'select';
  const showSelectCtrl = isSelectMode && selectionStyle === 'checkbox';
  const showActions = mode === 'manage' && !!rowActions;
  const primary = columns.find((c) => c.mobilePrimary) ?? columns[0];
  const details = columns.filter((c) => c.key !== primary?.key);

  if (rows.length === 0) {
    return isLoading ? null : <EmptyState message={emptyMessage} iconSize={40} />;
  }

  return (
    <>
      {rows.map((row) => {
        const id = rowKey(row);
        const checked = isSelectMode ? !!isSelected?.(id) : false;
        const onClick = isSelectMode && onToggleSelect ? () => onToggleSelect(row) : undefined;
        return (
          <MobileCardItem key={id} clickable={isSelectMode} onClick={onClick}>
            <MobilePrimaryRow>
              <div style={{ minWidth: 0, flex: 1 }}>{renderCellContent(primary, row)}</div>
              <div style={{ flexShrink: 0 }} onClick={(e) => e.stopPropagation()}>
                {showSelectCtrl && (
                  <ToggleControl
                    size="small"
                    checked={checked}
                    onChange={() => onToggleSelect?.(row)}
                  />
                )}
                {showActions && (
                  <Stack direction="row" spacing={0.25} sx={{ justifyContent: 'flex-end' }}>
                    {rowActions!(row)}
                  </Stack>
                )}
              </div>
            </MobilePrimaryRow>
            {details.length > 0 && (
              <Stack spacing={0.625} sx={{ mt: '0.875rem' }}>
                {details.map((col) => (
                  <MobileDetailRow key={col.key}>
                    <MobileDetailLabel>{col.label}</MobileDetailLabel>
                    <MobileDetailValue>{renderCellContent(col, row)}</MobileDetailValue>
                  </MobileDetailRow>
                ))}
              </Stack>
            )}
          </MobileCardItem>
        );
      })}
    </>
  );
}
