import { styled, type Theme } from '@mui/material/styles';

/**
 * 탭 표 셀에서 max-width 기반으로 ellipsis 잘라 보여주는 공용 span.
 * GenericList 의 `TruncatedCellContent` 는 table-layout: fixed 기반 (셀 폭 100%) 이라 동작 모델이 다름 — 탭 표는 컬럼 폭 hint 만 있어 max-width 명시가 필요.
 */
export const TruncatedSpan = styled('span', {
  shouldForwardProp: (prop) => prop !== 'maxWidth',
})<{ maxWidth: number }>(({ maxWidth }) => ({
  display: 'inline-block',
  maxWidth,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
  verticalAlign: 'bottom',
}));

/** 날짜/시각 등 줄바꿈을 막아야 하는 짧은 텍스트 셀. */
export const NowrapText = styled('span')({
  whiteSpace: 'nowrap',
});

export type StatusTone = 'primary' | 'success' | 'warning' | 'info' | 'disabled' | 'secondary';

function statusColor(tone: StatusTone, theme: Theme): string {
  switch (tone) {
    case 'primary':
      return theme.palette.primary.main;
    case 'success':
      return theme.palette.success.main;
    case 'warning':
      return theme.palette.warning.main;
    case 'info':
      return theme.palette.info.main;
    case 'disabled':
      return theme.palette.text.disabled;
    case 'secondary':
      return theme.palette.text.secondary;
  }
}

/**
 * 상태 텍스트 — 활성/주담당 등 강조는 fontWeight 500, 비활성/종료는 weight 없음.
 * chip/badge/dot 미사용 — 매트릭스 톤 유지를 위해 텍스트 색만 사용 (CLAUDE.md GenericTabbedTable 컬럼 설계 원칙).
 */
export const StatusText = styled('span', {
  shouldForwardProp: (prop) => prop !== 'tone',
})<{ tone: StatusTone }>(({ tone, theme }) => ({
  color: statusColor(tone, theme),
  fontWeight: tone === 'disabled' ? undefined : 500,
}));

/** 행 액션 아이콘 (수정/종료/삭제) 클러스터의 공용 wrapper. */
export const RowActions = styled('span')({
  display: 'inline-flex',
  justifyContent: 'flex-end',
  gap: '0.125rem',
});

/** 셀 내부 텍스트 링크 — 행 클릭과 분리해야 하는 inline 액션 (button 시맨틱 + 링크 톤). */
export const InlineLinkButton = styled('button')(({ theme }) => ({
  background: 'none',
  border: 'none',
  padding: 0,
  margin: 0,
  cursor: 'pointer',
  fontSize: 'inherit',
  fontWeight: 600,
  color: theme.palette.primary.main,
  textAlign: 'left',
  '&:hover': { textDecoration: 'underline' },
}));

/**
 * ISO datetime ("2026-04-20T10:00:00") → "2026-04-20 10:00" 표시 형식.
 * 탭 표의 일시 컬럼 / Detail 모달의 일시 항목 공용.
 */
export function formatDateTime(iso: string): string {
  return iso.replace('T', ' ').slice(0, 16);
}
