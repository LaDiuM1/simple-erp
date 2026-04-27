import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';

/**
 * 직각 박스 — 상단 보더는 의도적으로 없음 (상위 GenericHeaderDetails 의 bottom border 가
 * 시각적 상단선 역할). DetailRoot 에서 gap 0 으로 두 컴포넌트를 flush 로 이어 붙인다.
 */
export const TabbedRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  borderLeft: `1px solid ${theme.palette.divider}`,
  borderRight: `1px solid ${theme.palette.divider}`,
  borderBottom: `1px solid ${theme.palette.divider}`,
  display: 'flex',
  flexDirection: 'column',
  overflow: 'hidden',
}));

export const TabBar = styled('div')(({ theme }) => ({
  display: 'flex',
  alignItems: 'stretch',
  justifyContent: 'space-between',
  gap: '0.75rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  paddingRight: '0.5rem',
  minHeight: 44,
}));

export const TabBarLeft = styled('div')({
  display: 'flex',
  alignItems: 'stretch',
  flex: 1,
  minWidth: 0,
});

export const TabBarRight = styled('div')({
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  flexShrink: 0,
});

interface TabBtnProps {
  isActive: boolean;
}

/**
 * 탭 버튼 — MUI Tabs 미사용. font-weight 는 500 으로 고정 (활성 시 weight 변경에 따른 layout shift 회피).
 * 활성 단서는 텍스트 색을 `primary.main` 으로 강하게 띄우고 2px primary 하단 indicator 를 같이 깔아 컬러 + 라인 두 채널로 명확히.
 * `marginBottom: -1` 은 indicator 가 TabBar 하단 divider 위에 정확히 얹히도록 보정.
 */
export const TabButton = styled('button', {
  shouldForwardProp: (prop) => prop !== 'isActive',
})<TabBtnProps>(({ theme, isActive }) => ({
  background: 'none',
  border: 'none',
  cursor: 'pointer',
  padding: '0 1.25rem',
  fontSize: '0.875rem',
  fontWeight: 500,
  lineHeight: 1,
  letterSpacing: '-0.005em',
  color: isActive ? theme.palette.primary.main : theme.palette.text.disabled,
  borderBottom: '2px solid',
  borderBottomColor: isActive ? theme.palette.primary.main : 'transparent',
  marginBottom: -1,
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.375rem',
  whiteSpace: 'nowrap',
  transition: 'color 0.12s ease, border-bottom-color 0.12s ease',
  '&:hover': {
    color: isActive ? theme.palette.primary.main : theme.palette.text.primary,
  },
}));

/**
 * 탭 라벨 우측 카운트 — 라벨과 동일한 size / weight / letter-spacing / line-height 로 한 덩어리처럼.
 * 색만 `color: inherit` + 옅은 alpha 로 종속 (활성=옅은 primary, idle=옅은 disabled).
 * line-height 1 로 명시 — 라벨 텍스트 노드와 box 높이를 정확히 같게 맞춰 flex center 베이스라인 일치.
 */
export const TabCount = styled('span')({
  fontSize: '0.875rem',
  fontWeight: 500,
  lineHeight: 1,
  letterSpacing: '-0.005em',
  color: 'inherit',
  opacity: 0.5,
});

export const TabContent = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  minHeight: 0,
});

/**
 * 행 높이 통일 — 액션 버튼 (IconButton size=small ≈ 30px) 유무에 따라 행이 들쭉날쭉해지는 것을 방지.
 * `<td>` 에 height 를 주면 table row 가 가장 큰 셀에 맞춰 늘어나므로 모든 행이 동일 높이.
 * 38 = IconButton 30 + 셀 상하 padding 8.
 */
export const TabTableScrollArea = styled(Box)({
  width: '100%',
  overflowX: 'auto',
  '& tbody tr > td': {
    height: 38,
  },
});

/**
 * 모바일 카드 모드 — md 미만에서 표 대신 행 단위 카드로 노출.
 * 카드 내부는 `[label][value]` 2-col 매트릭스 (GenericDetailModal 와 동일 톤).
 */
export const MobileCardList = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
});

interface MobileCardProps {
  clickable?: boolean;
}

/**
 * 카드 1개 = 행 1개. 첫 카드는 top border 없음, 이후 카드는 2px divider 로 강한 분리.
 * 카드 내부 last row 의 cell border-bottom 은 제거되어 다음 카드의 top border 와 겹치지 않음.
 */
export const MobileCard = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'clickable',
})<MobileCardProps>(({ theme, clickable }) => ({
  display: 'grid',
  gridTemplateColumns: '108px minmax(0, 1fr)',
  cursor: clickable ? 'pointer' : 'default',
  transition: 'background-color 0.12s',
  '&:hover': {
    backgroundColor: clickable ? theme.palette.action.hover : 'transparent',
  },
  '&:not(:first-of-type)': {
    borderTop: `2px solid ${theme.palette.divider}`,
  },
  '& > :nth-last-of-type(-n+2)': {
    borderBottom: 'none',
  },
}));

export const MobileLabelCell = styled('div')(({ theme }) => ({
  padding: '0.5rem 0.875rem',
  backgroundColor: theme.palette.headerBg,
  borderRight: `1px solid ${theme.palette.divider}`,
  borderBottom: `1px solid ${theme.palette.divider}`,
  color: theme.palette.text.secondary,
  fontSize: '0.8125rem',
  fontWeight: 500,
  display: 'flex',
  alignItems: 'flex-start',
  minHeight: 40,
}));

export const MobileValueCell = styled('div')(({ theme }) => ({
  padding: '0.5rem 0.875rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  color: theme.palette.text.primary,
  fontSize: '0.875rem',
  display: 'flex',
  alignItems: 'flex-start',
  minHeight: 40,
  whiteSpace: 'pre-wrap',
  wordBreak: 'break-word',
}));

const tabActionButtonBase = {
  height: 30,
  paddingLeft: '0.75rem',
  paddingRight: '0.75rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  borderRadius: 0,
  textTransform: 'none' as const,
  letterSpacing: '-0.005em',
  boxShadow: 'none',
  '& .MuiButton-startIcon': { marginRight: '0.25rem' },
  '& .MuiButton-startIcon > *': { fontSize: '0.9375rem' },
};

/**
 * 탭 우측 슬롯 표준 secondary 액션 — PageHeader 의 cancel/secondary 톤과 동일한 직각 outline.
 * height 만 30 으로 살짝 낮춰 페이지 헤더 액션 (34) 과 위계 구분.
 */
export const TabActionButton = styled(Button)(({ theme }) => ({
  ...tabActionButtonBase,
  backgroundColor: 'transparent',
  color: theme.palette.text.secondary,
  border: `1px solid ${theme.palette.divider}`,
  '&:hover': {
    backgroundColor: theme.palette.background.default,
    borderColor: theme.palette.text.disabled,
    color: theme.palette.text.primary,
  },
  '&.Mui-disabled': {
    color: theme.palette.text.disabled,
    borderColor: theme.palette.divider,
  },
}));

/**
 * 탭 우측 슬롯 primary 액션 — PageHeader 의 design='create' / 'save' 와 동일 톤 (primary filled).
 * height 만 30 으로 살짝 낮춰 페이지 헤더 액션 (34) 과 위계 구분.
 */
export const TabPrimaryActionButton = styled(Button)(({ theme }) => ({
  ...tabActionButtonBase,
  fontWeight: 600,
  backgroundColor: theme.palette.primary.main,
  color: theme.palette.primary.contrastText,
  border: `1px solid ${theme.palette.primary.main}`,
  '&:hover': {
    backgroundColor: theme.palette.primary.dark,
    borderColor: theme.palette.primary.dark,
  },
  '&.Mui-disabled': {
    backgroundColor: theme.palette.primary.main,
    borderColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    opacity: 0.5,
  },
}));
