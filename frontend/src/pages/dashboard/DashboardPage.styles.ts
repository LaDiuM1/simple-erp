import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

export const DashboardRoot = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '2rem',
  width: '100%',
  maxWidth: 1480,
  margin: '0 auto',
  [theme.breakpoints.down('md')]: { gap: '1.5rem' },
  [theme.breakpoints.down('sm')]: { gap: '1.25rem' },
}));

export const DashboardGroup = styled('section')({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.875rem',
  minWidth: 0,
});

/** 핵심 지표는 한 줄에서 빠르게 비교하고, 좁은 화면에서만 단계적으로 접는다. */
export const KpiGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(4, minmax(0, 1fr))',
  gap: '0.75rem',
  [theme.breakpoints.down('lg')]: {
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
  },
  [theme.breakpoints.down('sm')]: {
    gridTemplateColumns: '1fr',
  },
}));

export const RecentGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
  gap: '0.875rem',
  [theme.breakpoints.down('md')]: {
    gridTemplateColumns: '1fr',
  },
}));

/** 계약 흐름을 주 화면으로 두고, 서비스 운영과 보증 일정을 보조 레일로 묶는다. */
export const OperationsGrid = styled(Box)({
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 430px), 1fr))',
  gap: '0.875rem',
  alignItems: 'start',
});

export const OperationsRail = styled(Box)({
  display: 'grid',
  gridTemplateColumns: 'minmax(0, 1fr)',
  gap: '0.875rem',
  minWidth: 0,
  '&:empty': { display: 'none' },
});
