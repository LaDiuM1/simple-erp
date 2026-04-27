import Box from '@mui/material/Box';
import { styled } from '@mui/material/styles';

export const PageRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  [theme.breakpoints.up('sm')]: { margin: '-2rem' },
}));

export const PageSurface = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '1.5rem',
  [theme.breakpoints.up('md')]: { padding: '2rem' },
}));

export const ContentBox = styled(Box)({
  width: '100%',
  maxWidth: 720,
  marginLeft: 'auto',
  marginRight: 'auto',
  display: 'flex',
  flexDirection: 'column',
  gap: '1.25rem',
});

export const InfoGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '120px 1fr',
  rowGap: '0.75rem',
  columnGap: '1rem',
  [theme.breakpoints.up('md')]: { gridTemplateColumns: '160px 1fr' },
}));

export const InfoLabel = styled('div')(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  paddingTop: '0.125rem',
}));

export const InfoValue = styled('div')(({ theme }) => ({
  fontSize: '0.875rem',
  color: theme.palette.text.primary,
  whiteSpace: 'pre-wrap',
  wordBreak: 'break-word',
  minHeight: '1.25rem',
}));

export const Mono = styled('span')({
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
});
