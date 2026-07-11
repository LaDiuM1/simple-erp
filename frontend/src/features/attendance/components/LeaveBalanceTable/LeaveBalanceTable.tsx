import IconButton from '@mui/material/IconButton';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Tooltip from '@mui/material/Tooltip';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import {
  BodyCell,
  BodyRow,
  EmptyState,
  HeaderCell,
  StyledTableContainer,
  TableScrollArea,
  TableWrapper,
  computeColumnWidths,
} from '@/shared/ui/GenericList';
import { RowActions } from '@/shared/ui/GenericTabbedTable';
import type { EmployeeLeaveBalance } from '@/features/attendance/types';

/**
 * 전 직원 잔여 연차 표 — BE 가 연도별 전체 배열 반환 (페이징 / 검색 / 삭제 없음).
 * GenericList styled primitives 만 재사용해 동일 시각 톤 유지.
 * 페이지 전체가 write 권한 게이트 안이라 부여 조정 액션 컬럼은 항상 노출.
 */
const TABLE_MIN_WIDTH = 560;
const ROW_HEIGHT = 44;

const COL_SPECS: ReadonlyArray<{ width?: number; flex?: number }> = [
  { flex: 1 },          // 직원
  { width: 96 },        // 부여
  { width: 96 },        // 사용
  { width: 96 },        // 잔여
  { width: 88 },        // 액션
];

const COL_WIDTHS = computeColumnWidths(COL_SPECS);

interface Props {
  rows: EmployeeLeaveBalance[];
  onAdjust: (balance: EmployeeLeaveBalance) => void;
}

export default function LeaveBalanceTable({ rows, onAdjust }: Props) {
  return (
    <TableWrapper>
      <TableScrollArea>
        <StyledTableContainer>
          <Table size="small" sx={{ tableLayout: 'fixed', width: '100%', minWidth: TABLE_MIN_WIDTH }}>
            <colgroup>
              {COL_WIDTHS.map((w, i) => (
                <col key={i} style={{ width: w }} />
              ))}
            </colgroup>
            <TableHead>
              <TableRow>
                <HeaderCell>직원</HeaderCell>
                <HeaderCell>부여</HeaderCell>
                <HeaderCell>사용</HeaderCell>
                <HeaderCell>잔여</HeaderCell>
                <HeaderCell align="right">액션</HeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.length === 0 ? (
                <TableRow>
                  <BodyCell colSpan={COL_SPECS.length} sx={{ p: 0 }}>
                    <EmptyState message="조회된 잔여 연차 정보가 없습니다." />
                  </BodyCell>
                </TableRow>
              ) : (
                rows.map((balance) => (
                  <BodyRow key={balance.employeeId} style={{ height: ROW_HEIGHT }}>
                    <BodyCell>{balance.employeeName}</BodyCell>
                    <BodyCell>{balance.grantedDays}일</BodyCell>
                    <BodyCell>{balance.usedDays}일</BodyCell>
                    <BodyCell>{balance.remainingDays}일</BodyCell>
                    <BodyCell align="right">
                      <RowActions>
                        <Tooltip title="부여 조정" arrow>
                          <IconButton
                            size="small"
                            aria-label="부여 조정"
                            onClick={() => onAdjust(balance)}
                            sx={{ '&:hover': { color: 'primary.main' } }}
                          >
                            <EditOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </RowActions>
                    </BodyCell>
                  </BodyRow>
                ))
              )}
            </TableBody>
          </Table>
        </StyledTableContainer>
      </TableScrollArea>
    </TableWrapper>
  );
}
