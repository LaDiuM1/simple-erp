import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';

export interface MenuPermissionState {
  canRead: boolean;
  canWrite: boolean;
}

export interface MenuPermissionBoundaryState extends MenuPermissionState {
  isLoading: boolean;
  isError: boolean;
  error: unknown;
  retry: () => void;
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

/** 라우트 진입 판단이 프로필 로딩 중 상태를 권한 없음으로 오판하지 않도록 조회 상태까지 노출한다. */
export function usePermissionBoundary(menuCode: string): MenuPermissionBoundaryState {
  const query = useGetMyProfileQuery();
  const matched = query.data?.menuPermissions.find((p) => p.menuCode === menuCode);

  return {
    canRead: matched?.canRead ?? false,
    canWrite: matched?.canWrite ?? false,
    isLoading: query.isLoading || (!query.data && !query.isError),
    isError: query.isError,
    error: query.error,
    retry: () => {
      void query.refetch();
    },
  };
}
