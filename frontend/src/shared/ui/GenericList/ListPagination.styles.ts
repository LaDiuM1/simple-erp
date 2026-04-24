import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Pagination from '@mui/material/Pagination';
import Typography from '@mui/material/Typography';

/** 목록 하단 페이지네이션 바 — surface 임베드. 상단 구분선 + 위쪽 그림자. */
export const PaginationBar = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.75rem',
  padding: '0.75rem 1.25rem',
  borderTop: `1px solid ${theme.palette.divider}`,
  boxShadow: '0 -2px 6px -2px rgba(15, 23, 42, 0.05)',
  flexShrink: 0,
  backgroundColor: theme.palette.background.paper,
  position: 'relative',
  zIndex: 1,
}));

/** "총 N건" 총 개수 텍스트. 모바일에서는 숨김. */
export const TotalCountText = styled(Typography)(({ theme }) => ({
  flex: 1,
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  display: 'none',
  [theme.breakpoints.up('sm')]: {
    display: 'block',
  },
}));

/** 우측 밸런스용 빈 공간 (pagination 중앙 정렬 느낌). 모바일에서는 숨김. */
export const PaginationSpacer = styled(Box)(({ theme }) => ({
  flex: 1,
  display: 'none',
  [theme.breakpoints.up('sm')]: {
    display: 'block',
  },
}));

/** MUI Pagination 커스터마이즈 — rounded, 작은 크기, 선택/호버/disabled 톤 조정. */
export const StyledPagination = styled(Pagination)(({ theme }) => ({
  flexShrink: 0,
  '& .MuiPagination-ul': { gap: '2px' },
  '& .MuiPaginationItem-root': {
    minWidth: 32,
    height: 32,
    margin: 0,
    fontSize: '0.8125rem',
    fontWeight: 500,
    color: theme.palette.text.secondary,
    border: 'none',
    borderRadius: '8px',
    transition: 'background-color 0.15s ease, color 0.15s ease, box-shadow 0.15s ease',
    '&:hover': {
      backgroundColor: 'rgba(15, 23, 42, 0.05)',
      color: theme.palette.text.primary,
    },
    '&.Mui-disabled': { opacity: 0.35 },
  },
  '& .MuiPaginationItem-ellipsis': {
    color: theme.palette.text.disabled,
    minWidth: 20,
    letterSpacing: '0.05em',
    '&:hover': { backgroundColor: 'transparent' },
  },
  '& .MuiPaginationItem-page.Mui-selected': {
    backgroundColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    fontWeight: 600,
    boxShadow: '0 1px 2px rgba(15, 23, 42, 0.12)',
    '&:hover': { backgroundColor: theme.palette.primary.dark },
  },
  '& .MuiPaginationItem-previousNext, & .MuiPaginationItem-firstLast': {
    color: theme.palette.text.secondary,
    '&:hover': {
      backgroundColor: 'rgba(15, 23, 42, 0.05)',
      color: theme.palette.text.primary,
    },
  },
  '& .MuiSvgIcon-root': { fontSize: '1.125rem' },
}));
