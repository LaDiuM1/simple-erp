import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Muted from '@/shared/ui/atoms/Muted';
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
import WithinRangeText from '@/features/attendance/components/WithinRangeText';
import { formatDateWithDay, formatTime } from '@/features/attendance/utils/format';
import type { Attendance } from '@/features/attendance/types';

/**
 * 내 월별 출퇴근 기록 표 — 페이징 / 검색 / 삭제가 없어 GenericList 부적합.
 * styled primitives 만 재사용해 동일 시각 톤 유지 (CodeRuleListPage 와 같은 경로).
 */
const TABLE_MIN_WIDTH = 560;
const ROW_HEIGHT = 44;

const COL_SPECS: ReadonlyArray<{ width?: number; flex?: number }> = [
  { width: 170 },       // 일자
  { flex: 1 },          // 출근 시각
  { flex: 1 },          // 퇴근 시각
  { flex: 1 },          // 출근 위치
  { flex: 1 },          // 퇴근 위치
];

const COL_WIDTHS = computeColumnWidths(COL_SPECS);

interface Props {
  rows: Attendance[];
}

export default function MonthlyAttendanceTable({ rows }: Props) {
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
                <HeaderCell>일자</HeaderCell>
                <HeaderCell>출근 시각</HeaderCell>
                <HeaderCell>퇴근 시각</HeaderCell>
                <HeaderCell>출근 위치</HeaderCell>
                <HeaderCell>퇴근 위치</HeaderCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.length === 0 ? (
                <TableRow>
                  <BodyCell colSpan={COL_SPECS.length} sx={{ p: 0 }}>
                    <EmptyState message="해당 월의 출퇴근 기록이 없습니다." />
                  </BodyCell>
                </TableRow>
              ) : (
                rows.map((attendance) => (
                  <BodyRow key={attendance.id} style={{ height: ROW_HEIGHT }}>
                    <BodyCell>{formatDateWithDay(attendance.workDate)}</BodyCell>
                    <BodyCell>{formatTime(attendance.checkInAt) ?? <Muted />}</BodyCell>
                    <BodyCell>{formatTime(attendance.checkOutAt) ?? <Muted />}</BodyCell>
                    <BodyCell>
                      {attendance.checkInAt
                        ? <WithinRangeText withinRange={attendance.checkInWithinRange} />
                        : <Muted />}
                    </BodyCell>
                    <BodyCell>
                      {attendance.checkOutAt
                        ? <WithinRangeText withinRange={attendance.checkOutWithinRange} />
                        : <Muted />}
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
