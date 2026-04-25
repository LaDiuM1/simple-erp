import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';

export interface MenuPermissionState {
  canRead: boolean;
  canWrite: boolean;
}

const NONE: MenuPermissionState = { canRead: false, canWrite: false };

/**
 * 현재 로그인한 직원의 메뉴 코드별 권한 상태를 반환.
 * 프로필이 아직 로드되지 않았거나 해당 메뉴 권한이 없으면 모두 false.
 */
export function usePermission(menuCode: string): MenuPermissionState {
  const { data: profile } = useGetMyProfileQuery();
  if (!profile) return NONE;

  const matched = profile.menuPermissions.find((p) => p.menuCode === menuCode);
  if (!matched) return NONE;

  return { canRead: matched.canRead, canWrite: matched.canWrite };
}
