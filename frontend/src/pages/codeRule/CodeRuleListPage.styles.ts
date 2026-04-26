import { styled } from '@mui/material/styles';
import { BodyRow } from '@/shared/ui/GenericList';

/** 행 전체 클릭 가능 — MUI 표준 action.hover 톤 + cursor pointer 로 클릭 가능성 가이드. */
export const ClickableRow = styled(BodyRow)(({ theme }) => ({
  cursor: 'pointer',
  '&:hover': {
    backgroundColor: theme.palette.action.hover,
  },
}));

export const PatternText = styled('span')(({ theme }) => ({
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
  fontSize: '0.8125rem',
  color: theme.palette.text.primary,
}));
