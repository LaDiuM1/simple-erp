import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import {
  BodyCell,
  BodyRow,
  EmptyState,
  HeaderCell,
  ListRoot,
  ListSurface,
  StyledTableContainer,
  TableScrollArea,
  TableWrapper,
} from '@/shared/ui/GenericList';
import { useCodeRuleListPage } from '@/features/codeRule/hooks/useCodeRuleListPage';
import { INPUT_MODE_LABEL } from '@/features/codeRule/types';
import { PatternText } from './CodeRuleListPage.styles';

/**
 * 코드 채번 규칙 목록 — enum 기반 정적 목록. GenericList styled primitives 만 재사용.
 */
const TABLE_MIN_WIDTH = 800;
const NO_COL_WIDTH = 64;
const ROW_HEIGHT = 44;

const COL_SPECS: ReadonlyArray<{ width?: number; flex?: number }> = [
  { width: 140 },       // 대상
  { flex: 2 },          // 패턴
  { width: 140 },       // 입력 방식
  { flex: 1 },          // 다음 코드
];

const TOTAL_FLEX = COL_SPECS.reduce(
  (sum, c) => (c.width != null ? sum : sum + (c.flex ?? 1)),
  0,
);
const COL_WIDTHS = COL_SPECS.map((c) =>
  c.width != null ? `${c.width}px` : `${(((c.flex ?? 1) / TOTAL_FLEX) * 100).toFixed(4)}%`,
);

export default function CodeRuleListPage() {
  const { queries, onRowClick } = useCodeRuleListPage();

  return (
    <QueryGate queries={queries}>
      {({ rules }) => (
        <ListRoot>
          <ListSurface>
            <TableWrapper>
              <TableScrollArea>
                <StyledTableContainer>
                  <Table size="small" sx={{ tableLayout: 'fixed', width: '100%', minWidth: TABLE_MIN_WIDTH }}>
                    <colgroup>
                      <col style={{ width: NO_COL_WIDTH }} />
                      {COL_WIDTHS.map((w, i) => (
                        <col key={i} style={{ width: w }} />
                      ))}
                    </colgroup>
                    <TableHead>
                      <TableRow>
                        <HeaderCell align="center">No</HeaderCell>
                        <HeaderCell>대상</HeaderCell>
                        <HeaderCell>패턴</HeaderCell>
                        <HeaderCell>입력 방식</HeaderCell>
                        <HeaderCell>다음 코드</HeaderCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {rules.length === 0 ? (
                        <TableRow>
                          <BodyCell colSpan={COL_SPECS.length + 1} sx={{ p: 0 }}>
                            <EmptyState message="등록된 채번 규칙이 없습니다." />
                          </BodyCell>
                        </TableRow>
                      ) : (
                        rules.map((rule, idx) => (
                          <BodyRow
                            key={rule.target}
                            clickable
                            onClick={() => onRowClick(rule)}
                            style={{ height: ROW_HEIGHT }}
                          >
                            <BodyCell align="center" sx={{ color: 'text.secondary' }}>
                              {idx + 1}
                            </BodyCell>
                            <BodyCell>
                              <Typography sx={{ fontSize: '0.875rem', fontWeight: 600 }}>
                                {rule.targetLabel}
                              </Typography>
                            </BodyCell>
                            <BodyCell>
                              <PatternText>{rule.pattern}</PatternText>
                            </BodyCell>
                            <BodyCell>{INPUT_MODE_LABEL[rule.inputMode]}</BodyCell>
                            <BodyCell>
                              {rule.nextCode ? (
                                <PatternText>{rule.nextCode}</PatternText>
                              ) : (
                                <Tooltip title="PARENT / 분류 토큰 사용 — 미리보기는 수정 화면에서">
                                  <Typography sx={{ color: 'text.disabled', fontSize: '0.875rem' }}>-</Typography>
                                </Tooltip>
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
          </ListSurface>
        </ListRoot>
      )}
    </QueryGate>
  );
}
