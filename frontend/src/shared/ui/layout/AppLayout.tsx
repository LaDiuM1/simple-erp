import { useCallback, useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import ButtonBase from '@mui/material/ButtonBase';
import Collapse from '@mui/material/Collapse';
import Typography from '@mui/material/Typography';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';
import { useAppDispatch } from '@/app/hooks';
import { logout } from '@/features/auth/store/authSlice';
import { useGetMyProfileQuery } from '@/features/member/api/memberApi';
import { MENU_CONFIG } from '@/shared/config/menuConfig';
import { getPageTitle } from '@/app/pageTitles';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import PageHeader from './PageHeader';
import {
  AppBarInner,
  BrandLink,
  BrandLogo,
  ChildNavItem,
  ContentColumn,
  HamburgerButton,
  HamburgerLine,
  LayoutBody,
  LayoutRoot,
  MainContent,
  MobileOverlay,
  NavGroup,
  NavGroupHeader,
  Sidebar,
  StyledAppBar,
} from './AppLayout.styles';

interface ProfileSummary {
  name: string;
  departmentName: string | null;
  positionName: string | null;
}

const initialExpandedGroups = () => new Set<string>(MENU_CONFIG.map((g) => g.code));

export default function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [logoutModalOpen, setLogoutModalOpen] = useState(false);
  const [pageHeaderActionsNode, setPageHeaderActionsNode] = useState<HTMLElement | null>(null);
  const [expandedGroups, setExpandedGroups] = useState<Set<string>>(initialExpandedGroups);

  const navigate = useNavigate();
  const dispatch = useAppDispatch();
  const location = useLocation();
  const snackbar = useSnackbar();
  const { data: profile } = useGetMyProfileQuery();

  const toggleGroup = (code: string) => {
    setExpandedGroups((prev) => {
      const next = new Set(prev);
      if (next.has(code)) next.delete(code);
      else next.add(code);
      return next;
    });
  };

  const handleActionsRef = useCallback((el: HTMLElement | null) => {
    setPageHeaderActionsNode(el);
  }, []);

  const pageTitle = getPageTitle(location.pathname);

  const handleLogout = () => {
    snackbar.success('로그아웃되었습니다.');
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
            {profile && <ProfileLink profile={profile} onClick={() => navigate('/member/me')} />}
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
            {MENU_CONFIG.map((parent) => {
              const children = (parent.children ?? []).filter(
                (child) => child.to && readableCodes.has(child.code),
              );

              if (children.length === 0) return null;

              const isExpanded = expandedGroups.has(parent.code);

              return (
                <NavGroup key={parent.code}>
                  <NavGroupHeader
                    type="button"
                    aria-expanded={isExpanded}
                    onClick={() => toggleGroup(parent.code)}
                  >
                    <span>{parent.name}</span>
                    <KeyboardArrowDownIcon
                      sx={{
                        fontSize: '1.125rem',
                        color: 'text.disabled',
                        transition: 'transform 0.2s',
                        transform: isExpanded ? 'rotate(0deg)' : 'rotate(-90deg)',
                      }}
                    />
                  </NavGroupHeader>
                  <Collapse in={isExpanded} timeout={180} unmountOnExit>
                    <Box>
                      {children.map((child) => (
                        <ChildNavItem key={child.code} to={child.to!} onClick={closeSidebar}>
                          {child.name}
                        </ChildNavItem>
                      ))}
                    </Box>
                  </Collapse>
                </NavGroup>
              );
            })}
          </nav>
        </Sidebar>

        <ContentColumn>
          <PageHeader
            title={pageTitle}
            actions={
              <Box
                ref={handleActionsRef}
                sx={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
              />
            }
          />
          <MainContent>
            <Outlet context={{ pageHeaderActionsNode }} />
          </MainContent>
        </ContentColumn>
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

/**
 * 헤더 우측에 노출되는 클릭 가능한 사용자 정보 영역.
 * 클릭 시 /member/me로 이동. 아이콘 없이 타이포그래피로만 정보 위계 표현.
 */
function ProfileLink({ profile, onClick }: { profile: ProfileSummary; onClick: () => void }) {
  return (
    <ButtonBase
      onClick={onClick}
      aria-label="내 정보"
      sx={{
        display: { xs: 'none', sm: 'flex' },
        flexDirection: 'column',
        alignItems: 'flex-start',
        px: '0.75rem',
        py: '0.3125rem',
        borderRadius: '6px',
        textAlign: 'left',
        transition: 'background-color 0.15s',
        '&:hover': { backgroundColor: 'rgba(15, 23, 42, 0.05)' },
      }}
    >
      <Typography
        component="span"
        sx={{ fontSize: '0.8125rem', fontWeight: 600, color: 'text.primary', lineHeight: 1.25 }}
      >
        {profile.name}
        {profile.positionName ? ` ${profile.positionName}` : ''}
      </Typography>
      {profile.departmentName && (
        <Typography
          component="span"
          sx={{ fontSize: '0.6875rem', color: 'text.secondary', lineHeight: 1.2, mt: '1px' }}
        >
          {profile.departmentName}
        </Typography>
      )}
    </ButtonBase>
  );
}
