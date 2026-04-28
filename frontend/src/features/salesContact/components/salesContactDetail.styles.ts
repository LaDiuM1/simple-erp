import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

/**
 * 영업 명부 상세 페이지의 외곽 컨테이너 — MainContent padding 을 음수 마진으로 escape 하여
 * GenericHeaderDetails 와 GenericTabbedTable 이 edge-to-edge 로 정렬되게 한다.
 * gap 0 — HeaderDetails 의 bottom divider 가 그대로 탭 박스의 상단 라인 역할.
 */
export const DetailRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  display: 'flex',
  flexDirection: 'column',
  [theme.breakpoints.up('sm')]: {
    margin: '-2rem',
  },
}));
