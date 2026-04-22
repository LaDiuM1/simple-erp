import { styled } from '@mui/material/styles';
import { AppBar, Box } from '@mui/material';
import { NavLink } from 'react-router-dom';

export const HEADER_HEIGHT = 64;
export const SIDEBAR_WIDTH = 240;

export const LayoutRoot = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  minHeight: '100vh',
});

export const StyledAppBar = styled(AppBar)(({ theme }) => ({
  height: HEADER_HEIGHT,
  backgroundColor: theme.palette.background.paper,
  color: theme.palette.text.primary,
  borderBottom: `1px solid ${theme.palette.divider}`,
  boxShadow: theme.shadows[1],
  position: 'sticky',
  top: 0,
  zIndex: theme.zIndex.appBar,
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
  '&:hover': { backgroundColor: theme.palette.background.default },
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
  backgroundColor: theme.palette.primary.main,
  borderRadius: 6,
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

export const MobileOverlay = styled(Box)(({ theme }) => ({
  position: 'fixed',
  inset: 0,
  backgroundColor: 'rgb(0 0 0 / 0.35)',
  zIndex: 99,
  [theme.breakpoints.up('md')]: { display: 'none' },
}));

export const Sidebar = styled(Box)(({ theme }) => ({
  width: SIDEBAR_WIDTH,
  backgroundColor: theme.palette.background.paper,
  borderRight: `1px solid ${theme.palette.divider}`,
  padding: '1.25rem 0.75rem',
  overflowY: 'auto',
  flexShrink: 0,
  [theme.breakpoints.down('md')]: {
    position: 'fixed',
    top: HEADER_HEIGHT,
    left: 0,
    bottom: 0,
    zIndex: 200,
    transform: 'translateX(-100%)',
    transition: 'transform 0.25s ease',
    '&.open': {
      transform: 'translateX(0)',
      boxShadow: theme.shadows[4],
    },
  },
}));

export const NavGroupTitle = styled('p')(({ theme }) => ({
  fontSize: '0.6875rem',
  fontWeight: 600,
  textTransform: 'uppercase',
  letterSpacing: '0.08em',
  color: theme.palette.text.disabled,
  padding: '0 0.75rem',
  marginTop: '1.25rem',
  marginBottom: '0.375rem',
}));

export const NavItem = styled(NavLink)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  padding: '0.625rem 0.75rem',
  borderRadius: 6,
  fontSize: '0.875rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
  textDecoration: 'none',
  transition: 'all 0.15s',
  marginBottom: '0.125rem',
  '&:hover': {
    backgroundColor: theme.palette.background.default,
    color: theme.palette.text.primary,
  },
  '&.active': {
    backgroundColor: theme.palette.primarySubtle,
    color: theme.palette.primary.main,
    fontWeight: 600,
  },
}));

export const ChildNavItem = styled(NavItem)({
  paddingLeft: '1.25rem',
});

export const MainContent = styled(Box)(({ theme }) => ({
  flex: 1,
  overflowY: 'auto',
  padding: '2rem',
  minWidth: 0,
  [theme.breakpoints.down('sm')]: {
    padding: '1rem',
  },
}));
