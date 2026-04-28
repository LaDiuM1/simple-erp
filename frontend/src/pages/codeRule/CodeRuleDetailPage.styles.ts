import Box from '@mui/material/Box';
import { styled } from '@mui/material/styles';

/**
 * 다른 도메인 상세 페이지(salesContact, salesCustomer, role 등)와 동일한 외곽 — MainContent padding 을
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

export const Mono = styled('span')({
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
});
