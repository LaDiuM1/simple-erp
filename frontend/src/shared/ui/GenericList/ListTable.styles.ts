import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableRow from '@mui/material/TableRow';
import TableSortLabel from '@mui/material/TableSortLabel';
import Typography from '@mui/material/Typography';

/** 테이블 + 로딩 오버레이를 담는 최외곽. position:relative 로 overlay 기준점 역할. */
export const TableWrapper = styled(Box)({
  position: 'relative',
  flex: 1,
  display: 'flex',
  flexDirection: 'column',
  minHeight: 0,
});

/** 내부 스크롤 영역. sticky 헤더와 함께 동작. */
export const TableScrollArea = styled(Box)({
  flex: 1,
  overflow: 'auto',
  minHeight: 0,
});

export const StyledTableContainer = styled(TableContainer)({
  backgroundColor: 'transparent',
  overflow: 'visible',
});

/** sticky 헤더 셀 (정렬 컬럼 + 행 액션 헤더 공용). 사이드바와 동일한 표준 회색 톤. */
export const HeaderCell = styled(TableCell)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: theme.palette.text.secondary,
  whiteSpace: 'nowrap',
  borderBottom: `1px solid ${theme.palette.divider}`,
  padding: '0.625rem 1.25rem',
  backgroundColor: '#F8FAFC',
  position: 'sticky',
  top: 0,
  zIndex: 2,
}));

export const StyledSortLabel = styled(TableSortLabel)(({ theme }) => ({
  color: 'inherit !important',
  '& .MuiTableSortLabel-icon': { fontSize: '1rem', opacity: 0.5 },
  '&.Mui-active .MuiTableSortLabel-icon': {
    opacity: 1,
    color: theme.palette.text.primary,
  },
}));

/**
 * 본문 행. hover 시 배경 변경 + 행 액션(opacity 0 → 1) 페이드인.
 * 마지막 행은 border-bottom 제거 (surface 바닥 구분선이 이미 있음).
 */
export const BodyRow = styled(TableRow)(({ theme }) => ({
  transition: 'background-color 0.12s',
  '&:hover': { backgroundColor: theme.palette.background.default },
  '&:hover .row-actions': { opacity: 1 },
  '& > td': {
    paddingLeft: '1.25rem',
    paddingRight: '1.25rem',
    borderBottom: `1px solid ${theme.palette.divider}`,
  },
  '&:last-child > td': {
    borderBottom: 'none',
  },
}));

export const BodyCell = styled(TableCell)(({ theme }) => ({
  fontSize: '0.875rem',
  color: theme.palette.text.primary,
  paddingTop: '0.625rem',
  paddingBottom: '0.625rem',
  borderBottom: 'none',
}));

/** 행 액션 셀 — 기본 투명, BodyRow hover 시 페이드인. className 'row-actions' 로 타겟팅됨. */
export const RowActionsCell = styled(TableCell)({
  paddingTop: '0.625rem',
  paddingBottom: '0.625rem',
  opacity: 0,
  transition: 'opacity 0.12s',
});

/** 빈 상태 안내 영역. */
export const EmptyStateContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  padding: '4rem 0',
  gap: '0.75rem',
  color: theme.palette.text.secondary,
}));

export const EmptyStateText = styled(Typography)({
  fontSize: '0.875rem',
});

/** 로딩 오버레이 — TableWrapper position:relative 기준 absolute. */
export const LoadingOverlayBox = styled(Box)({
  position: 'absolute',
  inset: 0,
  backgroundColor: 'rgba(255, 255, 255, 0.65)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  zIndex: 3,
});

/* --------------------------------------------------------------------------
 * Mobile (카드 리스트)
 * ------------------------------------------------------------------------ */

/**
 * 모바일 카드 아이템. 카드 사이에 top border 로 구분.
 * 첫 카드는 :first-of-type 로 border 제거.
 */
export const MobileCardItem = styled(Box)(({ theme }) => ({
  padding: '1rem 1.25rem',
  borderTop: `1px solid ${theme.palette.divider}`,
  transition: 'background-color 0.12s',
  '&:hover': { backgroundColor: theme.palette.background.default },
  '&:first-of-type': { borderTop: 'none' },
}));

export const MobilePrimaryRow = styled(Box)({
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'flex-start',
  gap: '0.5rem',
});

export const MobileDetailRow = styled(Box)({
  display: 'flex',
  justifyContent: 'space-between',
  gap: '1rem',
  alignItems: 'center',
});

export const MobileDetailLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  fontWeight: 500,
}));

export const MobileDetailValue = styled(Box)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.primary,
  textAlign: 'right',
}));
