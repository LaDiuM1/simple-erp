import { useNavigate } from 'react-router-dom';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import InboxIcon from '@mui/icons-material/InboxOutlined';
import { styled } from '@mui/material/styles';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import {
  BodyCell,
  BodyRow,
  EmptyStateContainer,
  EmptyStateText,
  HeaderCell,
  ListRoot,
  ListSurface,
  StyledTableContainer,
  TableScrollArea,
  TableWrapper,
} from '@/shared/ui/GenericList';
import { useGetCodeRulesQuery } from '@/features/codeRule/api/codeRuleApi';
import {
  INPUT_MODE_LABEL,
  RESET_POLICY_LABEL,
} from '@/features/codeRule/types';
import type { ApiError } from '@/shared/types/api';

/**
 * 코드 채번 규칙 목록.
 * <p>
 * 대상은 enum 으로 관리되어 항목 수가 적고 추가/삭제가 없으므로 GenericList 의 페이징·검색·삭제는 불필요.
 * 시각 톤은 GenericList 와 동일하게 styled primitives (ListRoot/ListSurface/HeaderCell/BodyCell/BodyRow) 를 재사용.
 * 행 전체 클릭으로 편집 진입 — 별도 액션 아이콘 없이 hover 효과로 가이드.
 */
export default function CodeRuleListPage() {
  const navigate = useNavigate();
  const { data: rules = [], isLoading, isError, refetch, error } = useGetCodeRulesQuery();

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />;

  return (
    <ListRoot>
      <ListSurface>
        <TableWrapper>
          <TableScrollArea>
            <StyledTableContainer>
              <Table size="small" sx={{ minWidth: 720 }}>
                <TableHead>
                  <TableRow>
                    <HeaderCell align="center" sx={{ width: 64 }}>No</HeaderCell>
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
                      <BodyCell colSpan={8} sx={{ p: 0 }}>
                        <EmptyStateContainer>
                          <InboxIcon sx={{ fontSize: 44, color: 'text.disabled' }} />
                          <EmptyStateText>등록된 채번 규칙이 없습니다.</EmptyStateText>
                        </EmptyStateContainer>
                      </BodyCell>
                    </TableRow>
                  ) : (
                    rules.map((rule, idx) => (
                      <ClickableRow
                        key={rule.target}
                        hover
                        onClick={() => navigate(`/code-rules/${rule.target}/edit`)}
                      >
                        <BodyCell align="center" sx={{ color: 'text.secondary', width: 64 }}>
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
                      </ClickableRow>
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

/** 행 전체 클릭 가능 — MUI 표준 action.hover 톤 + cursor pointer 로 클릭 가능성 가이드. */
const ClickableRow = styled(BodyRow)(({ theme }) => ({
  cursor: 'pointer',
  '&:hover': {
    backgroundColor: theme.palette.action.hover,
  },
}));

const PatternText = styled('span')(({ theme }) => ({
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
  fontSize: '0.8125rem',
  color: theme.palette.text.primary,
}));
