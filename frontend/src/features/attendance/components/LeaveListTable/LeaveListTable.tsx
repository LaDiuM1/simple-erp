import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Tooltip from '@mui/material/Tooltip';
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
import LeaveStatusIndicator from '@/features/attendance/components/LeaveStatusIndicator';
import {
  SurfaceHeaderRow,
  SurfaceRoot,
  SurfaceTitle,
} from '@/features/attendance/components/attendanceSurface.styles';
import { formatLeavePeriod } from '@/features/attendance/utils/format';
import { LEAVE_TYPE_LABELS, type Leave } from '@/features/attendance/types';
import { ReasonText } from './LeaveListTable.styles';

/**
 * 내 휴가 신청 목록 — BE 가 전체 리스트 반환 (페이징 / 검색 / 삭제 없음).
 * GenericList styled primitives 만 재사용해 동일 시각 톤 유지.
 * 결재 문서가 연결된 행은 클릭으로 결재 상세 진입.
 */
const TABLE_MIN_WIDTH = 720;
const ROW_HEIGHT = 44;

const COL_SPECS: ReadonlyArray<{ width?: number; flex?: number }> = [
  { width: 64 },        // No
  { width: 110 },       // 유형
  { flex: 1.4 },        // 기간
  { width: 88 },        // 일수
  { width: 96 },        // 상태
  { flex: 2 },          // 사유
];

const COL_WIDTHS = computeColumnWidths(COL_SPECS);

interface Props {
  rows: Leave[];
  /** 행 클릭 — 결재 문서 미연결 (approvalDocumentId null) 행은 클릭 불가로 렌더. */
  onRowClick: (leave: Leave) => void;
}

export default function LeaveListTable({ rows, onRowClick }: Props) {
  return (
    <SurfaceRoot>
      <SurfaceHeaderRow>
        <SurfaceTitle>신청 내역</SurfaceTitle>
      </SurfaceHeaderRow>
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
                  <HeaderCell align="center">No</HeaderCell>
                  <HeaderCell>유형</HeaderCell>
                  <HeaderCell>기간</HeaderCell>
                  <HeaderCell>일수</HeaderCell>
                  <HeaderCell>상태</HeaderCell>
                  <HeaderCell>사유</HeaderCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.length === 0 ? (
                  <TableRow>
                    <BodyCell colSpan={COL_SPECS.length} sx={{ p: 0 }}>
                      <EmptyState message="휴가 신청 내역이 없습니다." />
                    </BodyCell>
                  </TableRow>
                ) : (
                  rows.map((leave, idx) => (
                    <BodyRow
                      key={leave.id}
                      clickable={leave.approvalDocumentId != null}
                      onClick={
                        leave.approvalDocumentId != null ? () => onRowClick(leave) : undefined
                      }
                      style={{ height: ROW_HEIGHT }}
                    >
                      <BodyCell align="center" sx={{ color: 'text.secondary' }}>
                        {idx + 1}
                      </BodyCell>
                      <BodyCell>{LEAVE_TYPE_LABELS[leave.leaveType]}</BodyCell>
                      <BodyCell>{formatLeavePeriod(leave.startDate, leave.endDate)}</BodyCell>
                      <BodyCell>{leave.days}일</BodyCell>
                      <BodyCell>
                        <LeaveStatusIndicator status={leave.status} />
                      </BodyCell>
                      <BodyCell>
                        {leave.reason ? (
                          <Tooltip title={leave.reason}>
                            <ReasonText>{leave.reason}</ReasonText>
                          </Tooltip>
                        ) : (
                          <Muted />
                        )}
                      </BodyCell>
                    </BodyRow>
                  ))
                )}
              </TableBody>
            </Table>
          </StyledTableContainer>
        </TableScrollArea>
      </TableWrapper>
    </SurfaceRoot>
  );
}
