import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

/**
 * 설비 상세 페이지의 외곽 컨테이너 — MainContent padding 을 음수 마진으로 escape 하여
 * GenericHeaderDetails 가 edge-to-edge 로 정렬되게 한다.
 */
export const DetailRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  display: 'flex',
  flexDirection: 'column',
  [theme.breakpoints.up('sm')]: {
    margin: '-2rem',
  },
}));
