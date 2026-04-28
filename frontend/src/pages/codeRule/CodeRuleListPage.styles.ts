import { styled } from '@mui/material/styles';

export const PatternText = styled('span')(({ theme }) => ({
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
  fontSize: '0.8125rem',
  color: theme.palette.text.primary,
}));
