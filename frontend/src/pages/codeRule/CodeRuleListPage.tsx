import { useNavigate } from 'react-router-dom';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
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
import { useGetCodeRulesQuery } from '@/features/codeRule/api/codeRuleApi';
import { PatternText } from './CodeRuleListPage.styles';
import {
  INPUT_MODE_LABEL,
  RESET_POLICY_LABEL,
} from '@/features/codeRule/types';
import { getErrorMessage } from '@/shared/api/error';

/**
 * 코드 채번 규칙 목록.
 *
 * 대상은 enum 으로 관리되어 항목 수가 적고 추가/삭제가 없으므로 GenericList 의 페이징·검색·삭제는 불필요.
 * 시각 톤은 GenericList 와 완전히 일치하도록 동일 styled primitives + 동일 layout 옵션
 * (table-layout fixed / colgroup / useFillRowHeight / EmptyState / BodyRow clickable) 을 사용.
 */

const TABLE_MIN_WIDTH = 1008;
const NO_COL_WIDTH = 64;
/**
 * 행 높이 고정 (px). 항목 수가 적은 enum 목록이라 useFillRowHeight 로 컨테이너에 맞춰 늘리면
 * 행이 비정상적으로 커져 어색함 → comfortable medium 톤으로 고정.
 */
const ROW_HEIGHT = 44;

/**
 * GenericList.computeColumnWidths 와 동일한 산식으로 인라인 계산.
 * 고정 폭 컬럼은 px, 가변 컬럼은 flex 비율을 정규화한 percentage — fixed-table-layout 이 px 를 먼저 빼고 남은 공간에 percentage 분배.
 */
const COL_SPECS: ReadonlyArray<{ width?: number; flex?: number }> = [
  { width: 120 },       // 대상 — "부서 코드" 등 5자 내외 고정
  { width: 90 },        // 접두사 — "EMP-" 등 짧은 토큰
  { flex: 1.5 },        // 패턴 — 가장 가변적, monospace 길이 우선 분배
  { width: 130 },       // 입력 방식 — 최장 "선택 (자동/수동)" 8자
  { width: 100 },       // 초기화 — 최장 "초기화 없음" 5자
  { width: 120 },       // 부모별 시퀀스 — 헤더 "부모별 시퀀스" 6자 + center 패딩
  { flex: 1 },          // 다음 코드 — monospace, 패턴보다 짧음
];

const TOTAL_FLEX = COL_SPECS.reduce(
  (sum, c) => (c.width != null ? sum : sum + (c.flex ?? 1)),
  0,
);
const COL_WIDTHS = COL_SPECS.map((c) =>
  c.width != null ? `${c.width}px` : `${(((c.flex ?? 1) / TOTAL_FLEX) * 100).toFixed(4)}%`,
);

export default function CodeRuleListPage() {
  const navigate = useNavigate();
  const { data: rules = [], isLoading, isError, refetch, error } = useGetCodeRulesQuery();

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;

  return (
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
                    <HeaderCell>접두사</HeaderCell>
                    <HeaderCell>패턴</HeaderCell>
                    <HeaderCell>입력 방식</HeaderCell>
                    <HeaderCell>초기화</HeaderCell>
                    <HeaderCell align="center">부모별 시퀀스</HeaderCell>
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
                        onClick={() => navigate(`/code-rules/${rule.target}`)}
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
                        <BodyCell>{rule.prefix ?? '-'}</BodyCell>
                        <BodyCell>
                          <PatternText>{rule.pattern}</PatternText>
                        </BodyCell>
                        <BodyCell>{INPUT_MODE_LABEL[rule.inputMode]}</BodyCell>
                        <BodyCell>{RESET_POLICY_LABEL[rule.resetPolicy]}</BodyCell>
                        <BodyCell align="center">{rule.parentScoped ? '예' : '아니오'}</BodyCell>
                        <BodyCell>
                          {rule.nextCode ? (
                            <PatternText>{rule.nextCode}</PatternText>
                          ) : (
                            <Tooltip title="{PARENT} 토큰 사용 — 부모 코드별 미리보기는 편집 화면에서 확인">
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
  );
}
