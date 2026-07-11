import { styled } from '@mui/material/styles';

/** 사유 셀 truncate — 긴 사유는 말줄임 + Tooltip 으로 전체 노출. */
export const ReasonText = styled('span')({
  display: 'inline-block',
  maxWidth: '100%',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
  verticalAlign: 'bottom',
});
