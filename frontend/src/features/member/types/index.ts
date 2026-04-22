export interface MenuPermission {
  menuId: number;
  menuCode: string;
  canRead: boolean;
  canWrite: boolean;
}

export interface MemberProfileResponse {
  id: number;
  loginId: string;
  name: string;
  departmentName: string;
  positionName: string;
  roleName: string;
  roleCode: string;
  menuPermissions: MenuPermission[];
}
