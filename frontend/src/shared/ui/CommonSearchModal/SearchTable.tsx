import { useMemo, useRef } from 'react';
import Checkbox from '@mui/material/Checkbox';
import CircularProgress from '@mui/material/CircularProgress';
import Radio from '@mui/material/Radio';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import {
  BodyCell,
  BodyRow,
  HeaderCell,
  LoadingOverlayBox,
  StyledTableContainer,
  TableScrollArea,
  TableWrapper,
} from '@/shared/ui/GenericList/ListTable.styles';
import EmptyState from '@/shared/ui/GenericList/EmptyState';
import { renderTruncatableCell } from '@/shared/ui/GenericList/cellRender';
import { useFillRowHeight } from '@/shared/ui/GenericList/useFillRowHeight';
import type { ColumnConfig } from '@/shared/ui/GenericList';

interface Props<TRow> {
  rows: TRow[];
  columns: ColumnConfig<TRow>[];
  rowKey: (row: TRow) => number;
  isSelected: (id: number) => boolean;
  onToggleSelect: (row: TRow) => void;
  multiple: boolean;
  isLoading: boolean;
  emptyMessage: string;
  page: number;
  pageSize: number;
}

const NO_COL_WIDTH = 56;
const SELECT_COL_WIDTH = 48;

/**
 * 검색 모달 내부 테이블 — ListTable 보다 단순.
 * 행 액션(수정/삭제) 없음, 항상 클릭 가능, multiple 여부에 따라 Checkbox / Radio.
 */
export default function SearchTable<TRow>({
  rows,
  columns,
  rowKey,
  isSelected,
  onToggleSelect,
  multiple,
  isLoading,
  emptyMessage,
  page,
  pageSize,
}: Props<TRow>) {
  const ToggleControl = multiple ? Checkbox : Radio;
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('md'));
  const visibleColumns = useMemo(
    () => (isMobile ? columns.filter((c) => !c.hideOnMobile) : columns),
    [columns, isMobile],
  );

  const scrollAreaRef = useRef<HTMLDivElement>(null);
  /** 모달은 컨테이너가 클 수 있어 자동 채움이 너무 커지지 않도록 maxHeight 로 상한 적용. */
  const rowHeight = useFillRowHeight(scrollAreaRef, pageSize, { minHeight: 28, maxHeight: 56 });

  return (
    <TableWrapper>
      <TableScrollArea ref={scrollAreaRef}>
        <StyledTableContainer>
          <Table size="small" sx={{ minWidth: isMobile ? 0 : 560 }}>
            <TableHead>
              <TableRow>
                <HeaderCell align="center" padding="checkbox" sx={{ width: SELECT_COL_WIDTH }} />
                <HeaderCell align="center" sx={{ width: NO_COL_WIDTH }}>No</HeaderCell>
                {visibleColumns.map((col) => (
                  <HeaderCell key={col.key} align={col.align ?? 'left'} sx={{ width: col.width }}>
                    {col.label}
                  </HeaderCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.length === 0 ? (
                <TableRow>
                  <BodyCell colSpan={visibleColumns.length + 2} sx={{ p: 0 }}>
                    <EmptyState message={emptyMessage} iconSize={40} />
                  </BodyCell>
                </TableRow>
              ) : (
                rows.map((row, idx) => {
                  const id = rowKey(row);
                  const checked = isSelected(id);
                  return (
                    <BodyRow
                      key={id}
                      hover
                      selected={checked}
                      onClick={() => onToggleSelect(row)}
                      sx={{ cursor: 'pointer' }}
                      style={{ height: rowHeight }}
                    >
                      <BodyCell align="center" padding="checkbox" sx={{ width: SELECT_COL_WIDTH }}>
                        <ToggleControl
                          size="small"
                          checked={checked}
                          onChange={() => onToggleSelect(row)}
                          onClick={(e) => e.stopPropagation()}
                        />
                      </BodyCell>
                      <BodyCell align="center" sx={{ color: 'text.secondary', width: NO_COL_WIDTH }}>
                        {page * pageSize + idx + 1}
                      </BodyCell>
                      {visibleColumns.map((col) => (
                        <BodyCell key={col.key} align={col.align ?? 'left'}>
                          {renderTruncatableCell(col, row)}
                        </BodyCell>
                      ))}
                    </BodyRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </StyledTableContainer>
      </TableScrollArea>

      {isLoading && (
        <LoadingOverlayBox>
          <CircularProgress size={32} thickness={4} />
        </LoadingOverlayBox>
      )}
    </TableWrapper>
  );
}
