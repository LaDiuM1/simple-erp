import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

/**
 * 위젯 공용 통계 행 primitives — [라벨][건수][금액] 매트릭스 톤.
 * RecentSection 의 ItemRow 와 동일한 리듬이지만 클릭 없는 정적 행 변형.
 */
export const StatRow = styled('li')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: '0.75rem',
  padding: '0.375rem 0.5rem',
  margin: '0 -0.5rem',
  borderTop: `1px solid ${theme.palette.divider}`,
  '&:first-of-type': { borderTop: 'none' },
}));

export const StatLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.875rem',
  fontWeight: 500,
  color: theme.palette.text.primary,
  flex: 1,
  minWidth: 0,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
}));

export const StatCount = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  flexShrink: 0,
  fontVariantNumeric: 'tabular-nums',
}));

export const StatAmount = styled(Typography)(({ theme }) => ({
  fontSize: '0.875rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  flexShrink: 0,
  fontVariantNumeric: 'tabular-nums',
  minWidth: 110,
  textAlign: 'right',
}));

/** 섹션 하단 요약 (수금 vs 미수 등) — 상단 리스트와 divider 로 구분. */
export const SummaryFooter = styled(Box)(({ theme }) => ({
  borderTop: `1px solid ${theme.palette.divider}`,
  marginTop: '0.25rem',
  paddingTop: '0.5rem',
  display: 'flex',
  flexWrap: 'wrap',
  gap: '0.375rem 1.25rem',
}));

export const SummaryItem = styled(Box)({
  display: 'flex',
  alignItems: 'baseline',
  gap: '0.375rem',
});

export const SummaryLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
}));

export const SummaryValue = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'warning',
})<{ warning?: boolean }>(({ theme, warning }) => ({
  fontSize: '0.875rem',
  fontWeight: 600,
  color: warning ? theme.palette.warning.main : theme.palette.text.primary,
  fontVariantNumeric: 'tabular-nums',
}));

/** 섹션 내부 소제목 — 유형별 / 엔지니어별 구분. */
export const SubTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  fontWeight: 600,
  color: theme.palette.text.secondary,
  marginTop: '0.375rem',
}));
