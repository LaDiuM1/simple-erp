import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';

/** 부여 / 사용 / 잔여 3개 타일 — 데스크탑 3열 / 모바일 1열. */
export const BalanceGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
  gap: '1rem',
  [theme.breakpoints.down('sm')]: {
    gridTemplateColumns: '1fr',
  },
}));

/** 대시보드 KpiCard 와 동일 톤의 정적 타일 (클릭 없음). */
export const BalanceTile = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: '1rem',
  padding: '0.875rem 1.375rem',
  borderRadius: 12,
  border: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.paper,
}));

export const TileIcon = styled(Box)(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: 40,
  height: 40,
  borderRadius: 10,
  flexShrink: 0,
  color: theme.palette.primary.main,
  backgroundColor: theme.palette.primarySubtle,
}));

export const TileBody = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.125rem',
  minWidth: 0,
});

export const TileLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  fontWeight: 500,
  letterSpacing: '0.02em',
}));

export const TileValueRow = styled(Box)({
  display: 'flex',
  alignItems: 'baseline',
  gap: '0.25rem',
  marginTop: '0.125rem',
});

export const TileValue = styled(Typography)(({ theme }) => ({
  fontSize: '1.625rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
  letterSpacing: '-0.025em',
  lineHeight: 1.1,
  fontVariantNumeric: 'tabular-nums',
}));

export const TileUnit = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  fontWeight: 500,
}));

export const TileSuffix = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  color: theme.palette.text.disabled,
  marginTop: '0.125rem',
}));
