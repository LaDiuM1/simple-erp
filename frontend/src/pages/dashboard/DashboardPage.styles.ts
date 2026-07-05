import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

export const DashboardRoot = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.875rem',
  maxWidth: 1280,
  [theme.breakpoints.down('sm')]: { gap: '0.75rem' },
}));

/**
 * 벤토 그리드 12열 — 타일 크기 = 정보 중요도.
 * [히어로 차트 8][스탯 4] / [팔로업 5][타임라인 4][미니 3]
 */
export const BentoGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(12, minmax(0, 1fr))',
  gap: '0.75rem',
  [theme.breakpoints.down('lg')]: {
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
  },
  [theme.breakpoints.down('md')]: {
    gridTemplateColumns: '1fr',
  },
}));

export const HeroArea = styled(Box)(({ theme }) => ({
  gridColumn: 'span 8',
  display: 'flex',
  flexDirection: 'column',
  minWidth: 0,
  '& > *': { flex: 1 },
  [theme.breakpoints.down('lg')]: { gridColumn: 'span 2' },
  [theme.breakpoints.down('md')]: { gridColumn: 'span 1' },
}));

export const StatsColumn = styled(Box)(({ theme }) => ({
  gridColumn: 'span 4',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.75rem',
  minWidth: 0,
  [theme.breakpoints.down('lg')]: { gridColumn: 'span 2', flexDirection: 'row' },
  [theme.breakpoints.down('md')]: { gridColumn: 'span 1', flexDirection: 'column' },
}));

export const FollowUpArea = styled(Box)(({ theme }) => ({
  gridColumn: 'span 5',
  minWidth: 0,
  display: 'flex',
  flexDirection: 'column',
  '& > *': { flex: 1 },
  [theme.breakpoints.down('lg')]: { gridColumn: 'span 1' },
}));

export const TimelineArea = styled(Box)(({ theme }) => ({
  gridColumn: 'span 4',
  minWidth: 0,
  display: 'flex',
  flexDirection: 'column',
  '& > *': { flex: 1 },
  [theme.breakpoints.down('lg')]: { gridColumn: 'span 1' },
}));

export const MiniColumn = styled(Box)(({ theme }) => ({
  gridColumn: 'span 3',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.75rem',
  minWidth: 0,
  [theme.breakpoints.down('lg')]: { gridColumn: 'span 2', flexDirection: 'row' },
  [theme.breakpoints.down('md')]: { gridColumn: 'span 1', flexDirection: 'column' },
}));
