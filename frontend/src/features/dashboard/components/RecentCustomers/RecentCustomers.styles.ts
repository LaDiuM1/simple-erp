import { styled } from '@mui/material/styles';

export const CodeText = styled('span')(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.disabled,
  fontFamily: 'ui-monospace, SFMono-Regular, monospace',
  fontWeight: 500,
}));

export const TypeText = styled('span')(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
}));

export const MetaSeparator = styled('span')(({ theme }) => ({
  display: 'inline-block',
  width: 2,
  height: 2,
  borderRadius: '50%',
  margin: '0 0.5rem',
  backgroundColor: theme.palette.text.disabled,
  verticalAlign: 'middle',
}));
