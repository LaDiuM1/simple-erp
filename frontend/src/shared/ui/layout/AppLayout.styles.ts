import { styled } from '@mui/material/styles';
import { AppBar, Box } from '@mui/material';
import { NavLink } from 'react-router-dom';

export const HEADER_HEIGHT = 64;
export const SIDEBAR_WIDTH = 240;

export const LayoutRoot = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  height: '100vh',
  overflow: 'hidden',
});

export const StyledAppBar = styled(AppBar)(({ theme }) => ({
  height: HEADER_HEIGHT,
  flexShrink: 0,
  backgroundColor: theme.palette.background.paper,
  color: theme.palette.text.primary,
  borderBottom: `1px solid ${theme.palette.divider}`,
  boxShadow: 'none',
}));

export const AppBarInner = styled(Box)({
  height: '100%',
  display: 'flex',
  alignItems: 'center',
  padding: '0 1.5rem',
  gap: '1rem',
});

export const HamburgerButton = styled(Box)(({ theme }) => ({
  display: 'none',
  flexDirection: 'column',
  justifyContent: 'center',
  gap: 5,
  padding: '0.5rem',
  borderRadius: 6,
  cursor: 'pointer',
  transition: 'background-color 0.15s',
  flexShrink: 0,
  '&:hover': { backgroundColor: 'rgba(15, 23, 42, 0.06)' },
  [theme.breakpoints.down('md')]: { display: 'flex' },
}));

export const HamburgerLine = styled(Box)(({ theme }) => ({
  width: 20,
  height: 2,
  backgroundColor: theme.palette.text.secondary,
  borderRadius: 2,
}));

export const BrandLink = styled(NavLink)({
  display: 'flex',
  alignItems: 'center',
  gap: '0.75rem',
  textDecoration: 'none',
  flex: 1,
});

export const BrandLogo = styled(Box)(({ theme }) => ({
  width: 36,
  height: 36,
  backgroundColor: theme.palette.text.primary,
  borderRadius: 8,
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontSize: '0.75rem',
  fontWeight: 800,
  color: 'white',
  letterSpacing: '-0.5px',
  flexShrink: 0,
}));

export const LayoutBody = styled(Box)({
  display: 'flex',
  flex: 1,
  overflow: 'hidden',
  position: 'relative',
});

export const MobileOverlay = styled(Box)({
  position: 'fixed',
  inset: 0,
  backgroundColor: 'rgb(0 0 0 / 0.35)',
  zIndex: 99,
  '@media (min-width: 769px)': { display: 'none' },
});

export const Sidebar = styled(Box)(({ theme }) => ({
  width: SIDEBAR_WIDTH,
  backgroundColor: theme.palette.background.paper,
  borderRight: `1px solid ${theme.palette.divider}`,
  padding: '0.625rem 0.75rem 1.25rem',
  overflowY: 'auto',
  flexShrink: 0,
  position: 'relative',
  zIndex: 1,
  [theme.breakpoints.down('md')]: {
    position: 'fixed',
    top: HEADER_HEIGHT,
    left: 0,
    bottom: 0,
    zIndex: 200,
    transform: 'translateX(-100%)',
    transition: 'transform 0.25s ease',
    boxShadow: 'none',
    '&.open': {
      transform: 'translateX(0)',
      boxShadow: theme.shadows[4],
    },
  },
}));

export const NavGroup = styled(Box)({
  '& + &': { marginTop: '0.125rem' },
});

/**
 * 그룹 헤더 — Ink & White 톤의 uppercase 마이크로 라벨. 접기/펼치기 버튼 역할은 유지.
 */
export const NavGroupHeader = styled('button')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  width: '100%',
  padding: '0.875rem 0.625rem 0.375rem',
  border: 'none',
  borderRadius: 0,
  backgroundColor: 'transparent',
  fontSize: '0.6875rem',
  fontWeight: 700,
  letterSpacing: '0.1em',
  textTransform: 'uppercase',
  color: theme.palette.text.disabled,
  cursor: 'pointer',
  textAlign: 'left',
  fontFamily: 'inherit',
  transition: 'color 0.12s',
  '&:hover': { color: theme.palette.text.secondary },
  '&:focus-visible': {
    outline: `2px solid ${theme.palette.primary.light}`,
    outlineOffset: -2,
  },
}));

/**
 * 네비 항목 — active 는 배경 틴트 대신 잉크 텍스트 + 좌측 2px 잉크 바 (Ink & White).
 */
export const NavItem = styled(NavLink)(({ theme }) => ({
  position: 'relative',
  display: 'flex',
  alignItems: 'center',
  padding: '0.4375rem 0.625rem',
  margin: 0,
  borderRadius: 0,
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
  textDecoration: 'none',
  transition: 'color 0.12s',
  '&:hover': {
    color: theme.palette.text.primary,
  },
  '&.active': {
    color: theme.palette.text.primary,
    fontWeight: 650,
    '&::before': {
      content: '""',
      position: 'absolute',
      left: '-0.75rem',
      top: 5,
      bottom: 5,
      width: 2,
      backgroundColor: theme.palette.text.primary,
    },
  },
}));

export const ChildNavItem = styled(NavItem)({
  paddingLeft: '1.125rem',
  '&.active::before': {
    left: '-0.75rem',
  },
});

export const ContentColumn = styled(Box)({
  flex: 1,
  display: 'flex',
  flexDirection: 'column',
  minWidth: 0,
  overflow: 'hidden',
});

export const MainContent = styled('main')(({ theme }) => ({
  flex: 1,
  overflowY: 'auto',
  padding: '2rem',
  minWidth: 0,
  minHeight: 0,
  [theme.breakpoints.down('sm')]: {
    padding: '1rem',
  },
}));
