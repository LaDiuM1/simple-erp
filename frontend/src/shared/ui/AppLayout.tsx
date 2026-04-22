import { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { useAppDispatch } from '@/app/hooks';
import { logout } from '@/features/auth/store/authSlice';
import { useGetMyProfileQuery } from '@/features/member/api/memberApi';
import { MENU_CONFIG } from '@/shared/config/menuConfig';
import ConfirmModal from './ConfirmModal';
import {
  AppBarInner,
  BrandLink,
  BrandLogo,
  ChildNavItem,
  HamburgerButton,
  HamburgerLine,
  LayoutBody,
  LayoutRoot,
  MainContent,
  MobileOverlay,
  NavGroupTitle,
  NavItem,
  Sidebar,
  StyledAppBar,
} from './AppLayout.styles';

export default function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [logoutModalOpen, setLogoutModalOpen] = useState(false);
  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const { data: profile } = useGetMyProfileQuery();

  const handleLogout = () => {
    dispatch(logout());
    navigate('/login');
  };

  const closeSidebar = () => setSidebarOpen(false);

  const readableCodes = new Set(
    profile?.menuPermissions.filter((p) => p.canRead).map((p) => p.menuCode) ?? [],
  );

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
              <Typography
                sx={{
                  fontSize: '0.875rem',
                  fontWeight: 500,
                  color: 'text.secondary',
                  whiteSpace: 'nowrap',
                  display: { xs: 'none', sm: 'block' },
                }}
              >
                {[
                  profile.departmentName,
                  `${profile.name}${profile.positionName ? ` ${profile.positionName}님` : ''}`,
                ]
                  .filter(Boolean)
                  .join(' · ')}
              </Typography>
            )}
            <Button
              variant="outlined"
              size="small"
              onClick={() => setLogoutModalOpen(true)}
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
            <NavItem to="/member/me" onClick={closeSidebar}>
              내 정보
            </NavItem>

            {MENU_CONFIG.map((parent) => {
              const children = (parent.children ?? []).filter(
                (child) => child.to && readableCodes.has(child.code),
              );

              if (children.length === 0) return null;

              return (
                <Box key={parent.code}>
                  <NavGroupTitle>{parent.name}</NavGroupTitle>
                  {children.map((child) => (
                    <ChildNavItem key={child.code} to={child.to!} onClick={closeSidebar}>
                      {child.name}
                    </ChildNavItem>
                  ))}
                </Box>
              );
            })}
          </nav>
        </Sidebar>

        <MainContent component="main">
          <Outlet />
        </MainContent>
      </LayoutBody>

      <ConfirmModal
        isOpen={logoutModalOpen}
        title="로그아웃"
        message="정말 로그아웃 하시겠습니까?"
        confirmLabel="로그아웃"
        danger
        onConfirm={handleLogout}
        onCancel={() => setLogoutModalOpen(false)}
      />
    </LayoutRoot>
  );
}
