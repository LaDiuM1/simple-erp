import type { ReactNode } from 'react';
import Checkbox from '@mui/material/Checkbox';
import CircularProgress from '@mui/material/CircularProgress';
import Radio from '@mui/material/Radio';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import InboxIcon from '@mui/icons-material/InboxOutlined';
import Muted from '@/shared/ui/atoms/Muted';
import {
  BodyCell,
  BodyRow,
  EmptyStateContainer,
  EmptyStateText,
  HeaderCell,
  LoadingOverlayBox,
  StyledTableContainer,
  TableScrollArea,
  TableWrapper,
} from '@/shared/ui/GenericList/ListTable.styles';
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

  return (
    <TableWrapper>
      <TableScrollArea>
        <StyledTableContainer>
          <Table size="small" sx={{ minWidth: 560 }}>
            <TableHead>
              <TableRow>
                <HeaderCell align="center" padding="checkbox" sx={{ width: SELECT_COL_WIDTH }} />
                <HeaderCell align="center" sx={{ width: NO_COL_WIDTH }}>No</HeaderCell>
                {columns.map((col) => (
                  <HeaderCell key={col.key} align={col.align ?? 'left'} sx={{ width: col.width }}>
                    {col.label}
                  </HeaderCell>
                ))}
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.length === 0 ? (
                <TableRow>
                  <BodyCell colSpan={columns.length + 2} sx={{ p: 0 }}>
                    <EmptyStateContainer>
                      <InboxIcon sx={{ fontSize: 40, color: 'text.disabled' }} />
                      <EmptyStateText>{emptyMessage}</EmptyStateText>
                    </EmptyStateContainer>
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
                      {columns.map((col) => (
                        <BodyCell key={col.key} align={col.align ?? 'left'}>
                          {renderCell(col, row)}
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

function renderCell<TRow>(col: ColumnConfig<TRow>, row: TRow): ReactNode {
  if (col.render) return col.render(row);
  const value = (row as Record<string, unknown>)[col.key];
  if (value == null) return <Muted />;
  return value as ReactNode;
}
