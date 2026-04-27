import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

/**
 * 도메인 상세 페이지에서 여러 섹션 카드를 가로 2열로 배치하는 grid wrapper.
 * lg(1024px) 미만에서는 자동으로 세로 stack.
 * 자식 수가 홀수면 마지막 카드는 첫 번째 column 에만 배치 (나머지 grid cell 은 빈 자리).
 */
export const SideBySideGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '1.25rem',
  [theme.breakpoints.up('lg')]: {
    gridTemplateColumns: '1fr 1fr',
    gap: '1.5rem',
    alignItems: 'start',
  },
}));
