import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { styled } from '@mui/material/styles';

export const PanelRoot = styled(Box)(({ theme }) => ({
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 10,
  backgroundColor: theme.palette.background.paper,
  padding: '1.25rem 1.375rem 1.5rem',
  boxShadow: '0 2px 12px -4px rgba(15, 23, 42, 0.06)',
}));

export const PanelTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  fontWeight: 700,
  color: theme.palette.primary.main,
  textTransform: 'uppercase',
  letterSpacing: '0.08em',
  marginBottom: '0.875rem',
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  '&::before': {
    content: '""',
    display: 'inline-block',
    width: 4,
    height: 4,
    borderRadius: '50%',
    backgroundColor: theme.palette.primary.main,
  },
}));

/** 사람말 요약 — 미리보기 패널 헤드라인. 토큰 문법 노출 없이 줄글로 풀어 설명. */
export const SummaryBlock = styled(Typography)(({ theme }) => ({
  fontSize: '0.875rem',
  lineHeight: 1.6,
  color: theme.palette.text.primary,
  marginBottom: '1.125rem',
  paddingBottom: '1.125rem',
  borderBottom: `1px dashed ${theme.palette.divider}`,
}));

export const NextCodeRow = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.375rem',
  padding: '1rem 1.125rem',
  backgroundColor: theme.palette.primarySubtle,
  border: `1px solid ${theme.palette.primaryLight}`,
  borderRadius: 8,
  marginBottom: '1.125rem',
}));

export const NextCodeLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  fontWeight: 600,
  color: theme.palette.text.secondary,
  textTransform: 'uppercase',
  letterSpacing: '0.06em',
}));

export const NextCodeValue = styled(Typography)(({ theme }) => ({
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
  fontSize: '1.5rem',
  fontWeight: 700,
  color: theme.palette.primary.main,
  letterSpacing: '-0.01em',
  lineHeight: 1.1,
}));

export const SamplesLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  fontWeight: 600,
  color: theme.palette.text.disabled,
  textTransform: 'uppercase',
  letterSpacing: '0.06em',
  marginBottom: '0.5rem',
}));

export const SamplesList = styled('ul')(({ theme }) => ({
  listStyle: 'none',
  padding: 0,
  margin: 0,
  display: 'flex',
  flexDirection: 'column',
  '& li': {
    display: 'flex',
    alignItems: 'baseline',
    gap: '0.625rem',
    fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
    fontSize: '0.8125rem',
    color: theme.palette.text.primary,
    padding: '0.375rem 0.5rem',
    borderRadius: 4,
    transition: 'background-color 0.12s',
    '&:hover': {
      backgroundColor: theme.palette.background.default,
    },
  },
  '& li > .index': {
    fontFamily: 'inherit',
    color: theme.palette.text.disabled,
    minWidth: '1.5rem',
    fontSize: '0.75rem',
  },
}));

export const StatusText = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  padding: '0.5rem 0',
}));

export const ErrorText = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.error.main,
  padding: '0.5rem 0',
}));
