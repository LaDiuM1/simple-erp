import { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { useAppDispatch } from '@/app/hooks';
import { logout } from '@/features/auth/store/authSlice';
import { useGetMyProfileQuery } from '@/features/member/api/memberApi';
import {
  AppBarInner,
  BrandLink,
  BrandLogo,
  HamburgerButton,
  HamburgerLine,
  LayoutBody,
  LayoutRoot,
  MainContent,
  MobileOverlay,
  NavItem,
  Sidebar,
  StyledAppBar,
} from './AppLayout.styles';

const NAV_ITEMS = [
  { to: '/', label: '대시보드', end: true },
  { to: '/member/me', label: '내 정보', end: false },
];

export default function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { data: profile } = useGetMyProfileQuery();

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  const closeSidebar = () => setSidebarOpen(false);

  return (
    <LayoutRoot>
      <StyledAppBar>
        <AppBarInner>
          <HamburgerButton
            role="button"
            aria-label="메뉴 열기"
            onClick={() => setSidebarOpen((o) => !o)}
          >
            <HamburgerLine />
            <HamburgerLine />
            <HamburgerLine />
          </HamburgerButton>

          <BrandLink to="/">
            <BrandLogo>ERP</BrandLogo>
            <Typography sx={{ fontSize: '1.0625rem', fontWeight: 700, color: 'text.primary' }}>
              SIMPLE ERP
            </Typography>
          </BrandLink>

          <Box sx={{ display: 'flex', alignItems: 'center', gap: '1rem', flexShrink: 0 }}>
            {profile && (
              <Typography sx={{ fontSize: '0.875rem', fontWeight: 500, color: 'text.secondary' }}>
                {profile.name}
              </Typography>
            )}
            <Button
              variant="outlined"
              size="small"
              onClick={handleLogout}
              sx={{
                fontSize: '0.8125rem',
                fontWeight: 500,
                color: 'text.secondary',
                borderColor: 'divider',
                whiteSpace: 'nowrap',
                '&:hover': {
                  borderColor: 'primary.main',
                  color: 'primary.main',
                  backgroundColor: (theme) => theme.palette.primarySubtle,
                },
              }}
            >
              로그아웃
            </Button>
          </Box>
        </AppBarInner>
      </StyledAppBar>

      <LayoutBody>
        {sidebarOpen && <MobileOverlay onClick={closeSidebar} />}

        <Sidebar className={sidebarOpen ? 'open' : ''}>
          <nav>
            <p>메인 메뉴</p>
            {NAV_ITEMS.map(({ to, label, end }) => (
              <NavItem key={to} to={to} end={end} onClick={closeSidebar}>
                {label}
              </NavItem>
            ))}
          </nav>
        </Sidebar>

        <MainContent component="main">
          <Outlet />
        </MainContent>
      </LayoutBody>
    </LayoutRoot>
  );
}
