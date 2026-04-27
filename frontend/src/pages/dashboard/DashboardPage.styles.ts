import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

export const DashboardRoot = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '1.125rem',
  maxWidth: 1280,
  [theme.breakpoints.down('sm')]: { gap: '0.75rem' },
}));

/** 4개 KPI 카드 — 데스크탑 4열 / 태블릿 2열 / 모바일 1열. */
export const KpiGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(4, minmax(0, 1fr))',
  gap: '1rem',
  [theme.breakpoints.down('lg')]: {
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
  },
  [theme.breakpoints.down('sm')]: {
    gridTemplateColumns: '1fr',
  },
}));

/** 최근 고객사 / 최근 영업활동 — md 이상 2열 / 그 외 1열. */
export const RecentGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
  gap: '1rem',
  [theme.breakpoints.down('md')]: {
    gridTemplateColumns: '1fr',
  },
}));
