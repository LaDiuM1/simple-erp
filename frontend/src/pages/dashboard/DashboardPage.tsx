import type { ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import DashboardRoundedIcon from '@mui/icons-material/DashboardRounded';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import VerifiedUserRoundedIcon from '@mui/icons-material/VerifiedUserRounded';
import { useGetMyProfileQuery } from '@/features/member/api/memberApi';
import { MENU_CONFIG } from '@/shared/config/menuConfig';
import Muted from '@/shared/ui/atoms/Muted';
import type {
  MemberProfileResponse,
  MenuPermission,
} from '@/features/member/types';
import {
  DashboardRoot,
  ProfileGrid,
  ProfileLabel,
  ProfileRow,
  ProfileValue,
  RoleBadge,
  SectionCount,
  SectionHeader,
  SectionSurface,
  SectionTitle,
  ShortcutArrow,
  ShortcutBody,
  ShortcutEmpty,
  ShortcutIcon,
  ShortcutItem,
  ShortcutList,
  ShortcutName,
  ShortcutPath,
  WelcomeDate,
  WelcomeGreeting,
  WelcomeHeader,
  WelcomeText,
} from './DashboardPage.styles';

const SHORTCUT_ICONS: Record<string, ReactNode> = {
  MDM_HRM: <GroupsRoundedIcon sx={{ fontSize: 20 }} />,
};

const DEFAULT_SHORTCUT_ICON = <DashboardRoundedIcon sx={{ fontSize: 20 }} />;

export default function DashboardPage() {
  const { data: profile } = useGetMyProfileQuery();

  if (!profile) return null;

  const shortcuts = accessibleShortcuts(profile.menuPermissions);

  return (
    <DashboardRoot>
      <WelcomeSection profile={profile} />
      <ProfileSection profile={profile} />
      <ShortcutSection shortcuts={shortcuts} />
    </DashboardRoot>
  );
}

/* --------------------------------------------------------------------------
 * Welcome
 * ------------------------------------------------------------------------ */

function WelcomeSection({ profile }: { profile: MemberProfileResponse }) {
  const today = new Date().toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  });

  return (
    <WelcomeHeader>
      <WelcomeText>
        <WelcomeGreeting>
          안녕하세요, <strong>{profile.name}</strong> 님
        </WelcomeGreeting>
        <WelcomeDate>{today}</WelcomeDate>
      </WelcomeText>
      <RoleBadge>
        <VerifiedUserRoundedIcon sx={{ fontSize: 15 }} />
        {profile.roleName}
      </RoleBadge>
    </WelcomeHeader>
  );
}

/* --------------------------------------------------------------------------
 * Profile summary
 * ------------------------------------------------------------------------ */

function ProfileSection({ profile }: { profile: MemberProfileResponse }) {
  return (
    <SectionSurface>
      <SectionHeader>
        <SectionTitle>내 프로필</SectionTitle>
      </SectionHeader>
      <ProfileGrid>
        <ProfileRow>
          <ProfileLabel>아이디</ProfileLabel>
          <ProfileValue>{profile.loginId}</ProfileValue>
        </ProfileRow>
        <ProfileRow>
          <ProfileLabel>부서</ProfileLabel>
          <ProfileValue>{profile.departmentName ?? <Muted />}</ProfileValue>
        </ProfileRow>
        <ProfileRow>
          <ProfileLabel>직위</ProfileLabel>
          <ProfileValue>{profile.positionName ?? <Muted />}</ProfileValue>
        </ProfileRow>
        <ProfileRow>
          <ProfileLabel>권한</ProfileLabel>
          <ProfileValue>{profile.roleName}</ProfileValue>
        </ProfileRow>
      </ProfileGrid>
    </SectionSurface>
  );
}

/* --------------------------------------------------------------------------
 * Shortcuts (권한 기반 바로가기)
 * ------------------------------------------------------------------------ */

interface Shortcut {
  code: string;
  name: string;
  groupName: string;
  to: string;
  icon: ReactNode;
}

function accessibleShortcuts(permissions: MenuPermission[]): Shortcut[] {
  const readable = new Set(
    permissions.filter((p) => p.canRead).map((p) => p.menuCode),
  );
  const result: Shortcut[] = [];
  MENU_CONFIG.forEach((group) => {
    group.children?.forEach((child) => {
      if (child.to && readable.has(child.code)) {
        result.push({
          code: child.code,
          name: child.name,
          groupName: group.name,
          to: child.to,
          icon: SHORTCUT_ICONS[child.code] ?? DEFAULT_SHORTCUT_ICON,
        });
      }
    });
  });
  return result;
}

function ShortcutSection({ shortcuts }: { shortcuts: Shortcut[] }) {
  const navigate = useNavigate();

  return (
    <SectionSurface>
      <SectionHeader>
        <SectionTitle>
          바로가기
          <SectionCount>{shortcuts.length}</SectionCount>
        </SectionTitle>
      </SectionHeader>
      {shortcuts.length === 0 ? (
        <ShortcutEmpty>접근 가능한 메뉴가 없습니다.</ShortcutEmpty>
      ) : (
        <ShortcutList>
          {shortcuts.map((shortcut) => (
            <ShortcutItem
              key={shortcut.code}
              type="button"
              onClick={() => navigate(shortcut.to)}
            >
              <ShortcutIcon>{shortcut.icon}</ShortcutIcon>
              <ShortcutBody>
                <ShortcutName>{shortcut.name}</ShortcutName>
                <ShortcutPath>{shortcut.groupName}</ShortcutPath>
              </ShortcutBody>
              <ShortcutArrow>
                <ArrowForwardRoundedIcon sx={{ fontSize: 18 }} />
              </ShortcutArrow>
            </ShortcutItem>
          ))}
        </ShortcutList>
      )}
    </SectionSurface>
  );
}
