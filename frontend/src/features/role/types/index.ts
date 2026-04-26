import type { MenuCode } from '@/shared/config/menuConfig';

export interface MenuPermissionEntry {
  menuCode: MenuCode;
  canRead: boolean;
  canWrite: boolean;
}

export interface RoleSummary {
  id: number;
  code: string;
  name: string;
  description: string | null;
  system: boolean;
}

export interface RoleDetail {
  id: number;
  code: string;
  name: string;
  description: string | null;
  system: boolean;
  /** Menu enum 전체 행을 포함 (없는 항목도 canRead=false/canWrite=false 로). */
  menuPermissions: MenuPermissionEntry[];
}

export interface RoleCreateRequest {
  code: string;
  name: string;
  description: string | null;
  menuPermissions: MenuPermissionEntry[];
}

export interface RoleUpdateRequest {
  name: string;
  description: string | null;
  menuPermissions: MenuPermissionEntry[];
}

export interface RoleSearchParams {
  keyword?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

export type RoleListFilters = Omit<RoleSearchParams, 'page' | 'size' | 'sort'>;


// Form

export interface RoleFormValues {
  code: string;
  name: string;
  description: string;
  /** menuCode → {canRead, canWrite}. system role 의 경우 readonly. */
  permissions: Record<string, { canRead: boolean; canWrite: boolean }>;
}

export function emptyPermissions(menus: MenuCode[]): RoleFormValues['permissions'] {
  const acc: RoleFormValues['permissions'] = {};
  for (const m of menus) acc[m] = { canRead: false, canWrite: false };
  return acc;
}

export function emptyRoleForm(menus: MenuCode[]): RoleFormValues {
  return {
    code: '',
    name: '',
    description: '',
    permissions: emptyPermissions(menus),
  };
}

export function roleDetailToFormValues(d: RoleDetail, menus: MenuCode[]): RoleFormValues {
  const permissions = emptyPermissions(menus);
  for (const p of d.menuPermissions) {
    permissions[p.menuCode] = { canRead: p.canRead, canWrite: p.canWrite };
  }
  return {
    code: d.code,
    name: d.name,
    description: d.description ?? '',
    permissions,
  };
}

function permissionsToList(
  permissions: RoleFormValues['permissions'],
  menus: MenuCode[],
): MenuPermissionEntry[] {
  return menus.map((m) => {
    const p = permissions[m] ?? { canRead: false, canWrite: false };
    // 쓰기 true 면 읽기도 자동 true (BE 도 동일 보정하지만 요청 단계에서 정합)
    return {
      menuCode: m,
      canRead: p.canRead || p.canWrite,
      canWrite: p.canWrite,
    };
  });
}

export function roleFormToCreateRequest(
  v: RoleFormValues,
  menus: MenuCode[],
): RoleCreateRequest {
  const trimmedDescription = v.description.trim();
  return {
    code: v.code.trim(),
    name: v.name.trim(),
    description: trimmedDescription === '' ? null : trimmedDescription,
    menuPermissions: permissionsToList(v.permissions, menus),
  };
}

export function roleFormToUpdateRequest(
  v: RoleFormValues,
  menus: MenuCode[],
): RoleUpdateRequest {
  const trimmedDescription = v.description.trim();
  return {
    name: v.name.trim(),
    description: trimmedDescription === '' ? null : trimmedDescription,
    menuPermissions: permissionsToList(v.permissions, menus),
  };
}
