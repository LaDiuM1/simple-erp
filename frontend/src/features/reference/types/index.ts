export interface DepartmentInfo {
  id: number;
  code: string;
  name: string;
  parentId: number | null;
}

export interface PositionInfo {
  id: number;
  code: string;
  name: string;
}

export interface RoleInfo {
  id: number;
  code: string;
  name: string;
  description: string | null;
}
