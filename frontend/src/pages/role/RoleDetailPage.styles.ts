import Box from '@mui/material/Box';
import { styled } from '@mui/material/styles';

/**
 * 다른 도메인 상세 페이지(salesContact, salesCustomer 등)와 동일한 외곽 — MainContent padding 을
 * 음수 마진으로 escape 하여 GenericHeaderDetails 가 edge-to-edge 로 sticky 동작.
 */
export const DetailRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  display: 'flex',
  flexDirection: 'column',
  [theme.breakpoints.up('sm')]: {
    margin: '-2rem',
  },
}));

/** 헤더 디테일스 아래 본문 영역 — 매트릭스가 너무 wide 하지 않도록 좌우 패딩. */
export const MatrixSection = styled(Box)(({ theme }) => ({
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '1.25rem',
  [theme.breakpoints.up('md')]: {
    padding: '2rem',
  },
}));

export const SystemBadge = styled('span')(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.25rem',
  fontSize: '0.6875rem',
  fontWeight: 600,
  color: theme.palette.primary.main,
  backgroundColor: theme.palette.primarySubtle,
  padding: '0.125rem 0.5rem',
  borderRadius: 4,
  marginLeft: '0.5rem',
}));
