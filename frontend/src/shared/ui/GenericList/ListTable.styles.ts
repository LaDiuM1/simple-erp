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

/**
 * sticky 헤더 셀 (정렬 컬럼 헤더). 사이드바와 동일한 표준 회색 톤.
 * 헤더는 라벨이 짧고 의미 식별이 중요해 ellipsis 미적용 — 컬럼 폭이 좁아도 라벨이 보이도록 nowrap 만 유지.
 */
export const HeaderCell = styled(TableCell)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: theme.palette.text.secondary,
  whiteSpace: 'nowrap',
  borderBottom: `1px solid ${theme.palette.divider}`,
  padding: '0.625rem 1.25rem',
  backgroundColor: theme.palette.headerBg,
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
 * 본문 행. hover 시 배경 변경. 마지막 행은 border-bottom 제거 (surface 바닥 구분선이 이미 있음).
 * `clickable` 가 true 면 cursor pointer + hover 강조로 클릭 가능성 가이드.
 */
export const BodyRow = styled(TableRow, {
  shouldForwardProp: (prop) => prop !== 'clickable',
})<{ clickable?: boolean }>(({ theme, clickable }) => ({
  transition: 'background-color 0.12s',
  cursor: clickable ? 'pointer' : 'default',
  '&:hover': {
    backgroundColor: clickable ? theme.palette.action.hover : theme.palette.background.default,
  },
  '& > td': {
    paddingLeft: '1.25rem',
    paddingRight: '1.25rem',
    borderBottom: `1px solid ${theme.palette.divider}`,
  },
  '&:last-child > td': {
    borderBottom: 'none',
  },
}));

/**
 * 셀 상하 padding — 작은 고정값 (intrinsic ≈ 23px, MUI small 사이즈보다도 작음).
 * 행 높이는 useFillRowHeight 가 단독으로 결정 — padding 으로 행이 강제 확장되지 않도록 의도적으로 낮게 둠.
 * 큰 화면에서 행이 커지면 content 는 자동 vertical-align: middle 로 가운데 정렬.
 */
export const BodyCell = styled(TableCell)(({ theme }) => ({
  fontSize: '0.875rem',
  color: theme.palette.text.primary,
  paddingTop: '0.25rem',
  paddingBottom: '0.25rem',
  borderBottom: 'none',
}));

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

/**
 * 모바일 카드 아이템. 카드 사이에 top border 로 구분.
 * 첫 카드는 :first-of-type 로 border 제거. `clickable` 시 cursor pointer + hover 강조.
 */
export const MobileCardItem = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'clickable',
})<{ clickable?: boolean }>(({ theme, clickable }) => ({
  padding: '1rem 1.25rem',
  borderTop: `1px solid ${theme.palette.divider}`,
  transition: 'background-color 0.12s',
  cursor: clickable ? 'pointer' : 'default',
  '&:hover': {
    backgroundColor: clickable ? theme.palette.action.hover : theme.palette.background.default,
  },
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
