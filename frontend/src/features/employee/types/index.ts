export interface MenuPermission {
  menuCode: string;
  canRead: boolean;
  canWrite: boolean;
}

export interface EmployeeProfileResponse {
  id: number;
  loginId: string;
  name: string;
  departmentName: string | null;
  positionName: string | null;
  roleName: string;
  roleCode: string;
  menuPermissions: MenuPermission[];
}

export type EmployeeStatus = 'ACTIVE' | 'LEAVE' | 'RESIGNED';

export const EMPLOYEE_STATUS_LABELS: Record<EmployeeStatus, string> = {
  ACTIVE: '재직',
  LEAVE: '휴직',
  RESIGNED: '퇴사',
};

export const EMPLOYEE_STATUS_OPTIONS: { value: EmployeeStatus; label: string }[] = [
  { value: 'ACTIVE', label: '재직' },
  { value: 'LEAVE', label: '휴직' },
  { value: 'RESIGNED', label: '퇴사' },
];

export interface EmployeeSummary {
  id: number;
  loginId: string;
  name: string;
  departmentName: string | null;
  positionName: string | null;
  roleName: string | null;
  email: string | null;
  phone: string | null;
  joinDate: string | null;
  status: EmployeeStatus;
}

export interface EmployeeDetail {
  id: number;
  loginId: string;
  name: string;
  email: string | null;
  phone: string | null;
  zipCode: string | null;
  roadAddress: string | null;
  detailAddress: string | null;
  joinDate: string | null;
  status: EmployeeStatus;
  departmentId: number | null;
  departmentName: string | null;
  positionId: number | null;
  positionName: string | null;
  roleId: number;
  roleName: string;
}

export interface EmployeeCreateRequest {
  loginId: string;
  password: string;
  name: string;
  email?: string | null;
  phone?: string | null;
  zipCode?: string | null;
  roadAddress?: string | null;
  detailAddress?: string | null;
  joinDate?: string | null;
  status: EmployeeStatus;
  roleId: number;
  departmentId?: number | null;
  positionId?: number | null;
}

export interface EmployeeUpdateRequest {
  name: string;
  email?: string | null;
  phone?: string | null;
  zipCode?: string | null;
  roadAddress?: string | null;
  detailAddress?: string | null;
  joinDate?: string | null;
  status: EmployeeStatus;
  roleId: number;
  departmentId?: number | null;
  positionId?: number | null;
  /** 비어있으면 비밀번호 유지. 채워지면 새 비밀번호로 변경. */
  newPassword?: string | null;
}

export interface EmployeeSearchParams {
  keyword?: string | null;
  departmentId?: number | null;
  positionId?: number | null;
  roleId?: number | null;
  status?: EmployeeStatus | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type EmployeeListFilters = Omit<EmployeeSearchParams, 'page' | 'size' | 'sort'>;

export interface EmployeeFormValues {
  loginId: string;
  password: string;
  /** 등록 화면 전용 — 비밀번호 일치 확인용. 서버 전송 X */
  passwordConfirm: string;
  name: string;
  email: string;
  phone: string;
  zipCode: string;
  roadAddress: string;
  detailAddress: string;
  joinDate: string;
  status: EmployeeStatus;
  roleId: string;
  departmentId: string;
  positionId: string;
}

export const EMPTY_EMPLOYEE_FORM: EmployeeFormValues = {
  loginId: '',
  password: '',
  passwordConfirm: '',
  name: '',
  email: '',
  phone: '',
  zipCode: '',
  roadAddress: '',
  detailAddress: '',
  joinDate: '',
  status: 'ACTIVE',
  roleId: '',
  departmentId: '',
  positionId: '',
};

export function employeeDetailToFormValues(d: EmployeeDetail): EmployeeFormValues {
  return {
    loginId: d.loginId,
    password: '',
    passwordConfirm: '',
    name: d.name,
    email: d.email ?? '',
    phone: d.phone ?? '',
    zipCode: d.zipCode ?? '',
    roadAddress: d.roadAddress ?? '',
    detailAddress: d.detailAddress ?? '',
    joinDate: d.joinDate ?? '',
    status: d.status,
    roleId: String(d.roleId),
    departmentId: d.departmentId == null ? '' : String(d.departmentId),
    positionId: d.positionId == null ? '' : String(d.positionId),
  };
}

export function employeeFormToCreateRequest(v: EmployeeFormValues): EmployeeCreateRequest {
  return {
    loginId: v.loginId,
    password: v.password,
    name: v.name,
    email: emptyToNull(v.email),
    phone: emptyToNull(v.phone),
    zipCode: emptyToNull(v.zipCode),
    roadAddress: emptyToNull(v.roadAddress),
    detailAddress: emptyToNull(v.detailAddress),
    joinDate: emptyToNull(v.joinDate),
    status: v.status,
    roleId: Number(v.roleId),
    departmentId: v.departmentId === '' ? null : Number(v.departmentId),
    positionId: v.positionId === '' ? null : Number(v.positionId),
  };
}

export function employeeFormToUpdateRequest(v: EmployeeFormValues): EmployeeUpdateRequest {
  return {
    name: v.name,
    email: emptyToNull(v.email),
    phone: emptyToNull(v.phone),
    zipCode: emptyToNull(v.zipCode),
    roadAddress: emptyToNull(v.roadAddress),
    detailAddress: emptyToNull(v.detailAddress),
    joinDate: emptyToNull(v.joinDate),
    status: v.status,
    roleId: Number(v.roleId),
    departmentId: v.departmentId === '' ? null : Number(v.departmentId),
    positionId: v.positionId === '' ? null : Number(v.positionId),
    newPassword: v.password === '' ? null : v.password,
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v;
}
