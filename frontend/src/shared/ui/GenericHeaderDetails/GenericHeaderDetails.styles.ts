import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

/**
 * MainContent 스크롤 시 본문 최상단에 sticky 고정 — PageHeader bottom 과 딱 붙도록.
 * <p>
 * sticky `top` 은 scroll 컨테이너(MainContent) 의 padding box 기준이라, MainContent padding-top
 * 만큼 아래에 stick 되는 문제가 있다. DetailRoot 의 음수 margin (스크롤 안 한 상태에서 padding 상쇄)
 * 과 일치시키려면 sticky top 도 padding-top 의 음수값이어야 한다.
 * MainContent padding: xs=1rem, sm+=2rem.
 */
export const HeaderDetailsRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  borderBottom: `1px solid ${theme.palette.divider}`,
  display: 'flex',
  flexDirection: 'column',
  position: 'sticky',
  top: '-1rem',
  [theme.breakpoints.up('sm')]: {
    top: '-2rem',
  },
  zIndex: 2,
  boxShadow: '0 2px 6px -1px rgba(15, 23, 42, 0.05)',
}));

interface TableProps {
  columns: number;
}

export const HeaderDetailsTable = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'columns',
})<TableProps>(({ theme, columns }) => ({
  display: 'grid',
  gridTemplateColumns: '128px 1fr',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: `repeat(${columns}, 140px minmax(0, 1fr))`,
  },
}));

export const HeaderLabelCell = styled('div')(({ theme }) => ({
  padding: '0.625rem 1rem',
  borderRight: `1px solid ${theme.palette.divider}`,
  borderBottom: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.headerBg,
  color: theme.palette.text.secondary,
  fontSize: '0.8125rem',
  fontWeight: 500,
  display: 'flex',
  alignItems: 'center',
  minHeight: 44,
}));

export const HeaderValueCell = styled('div')(({ theme }) => ({
  padding: '0.625rem 1rem',
  borderRight: `1px solid ${theme.palette.divider}`,
  borderBottom: `1px solid ${theme.palette.divider}`,
  color: theme.palette.text.primary,
  fontSize: '0.875rem',
  display: 'flex',
  alignItems: 'center',
  minHeight: 44,
  whiteSpace: 'pre-wrap',
  wordBreak: 'break-word',
}));
