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
  departmentName: string | null;
  positionName: string | null;
  roleName: string;
  roleCode: string;
  menuPermissions: MenuPermission[];
}

export type MemberStatus = 'ACTIVE' | 'LEAVE' | 'RESIGNED';

export const MEMBER_STATUS_LABELS: Record<MemberStatus, string> = {
  ACTIVE: '재직',
  LEAVE: '휴직',
  RESIGNED: '퇴사',
};

export const MEMBER_STATUS_OPTIONS: { value: MemberStatus; label: string }[] = [
  { value: 'ACTIVE', label: '재직' },
  { value: 'LEAVE', label: '휴직' },
  { value: 'RESIGNED', label: '퇴사' },
];

export interface MemberSummary {
  id: number;
  loginId: string;
  name: string;
  departmentName: string | null;
  positionName: string | null;
  roleName: string | null;
  email: string | null;
  phone: string | null;
  joinDate: string | null;
  status: MemberStatus;
}

export interface MemberDetail {
  id: number;
  loginId: string;
  name: string;
  email: string | null;
  phone: string | null;
  zipCode: string | null;
  roadAddress: string | null;
  detailAddress: string | null;
  joinDate: string | null;
  status: MemberStatus;
  departmentId: number | null;
  departmentName: string | null;
  positionId: number | null;
  positionName: string | null;
  roleId: number;
  roleName: string;
}

export interface MemberCreateRequest {
  loginId: string;
  password: string;
  name: string;
  email?: string | null;
  phone?: string | null;
  zipCode?: string | null;
  roadAddress?: string | null;
  detailAddress?: string | null;
  joinDate?: string | null;
  status: MemberStatus;
  roleId: number;
  departmentId?: number | null;
  positionId?: number | null;
}

export interface MemberUpdateRequest {
  name: string;
  email?: string | null;
  phone?: string | null;
  zipCode?: string | null;
  roadAddress?: string | null;
  detailAddress?: string | null;
  joinDate?: string | null;
  status: MemberStatus;
  roleId: number;
  departmentId?: number | null;
  positionId?: number | null;
}

export interface MemberSearchParams {
  keyword?: string | null;
  departmentId?: number | null;
  positionId?: number | null;
  roleId?: number | null;
  status?: MemberStatus | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type MemberListFilters = Omit<MemberSearchParams, 'page' | 'size' | 'sort'>;

/* --------------------------------------------------------------------------
 * Form (등록/수정 페이지) — MUI 호환을 위해 전부 string 보관
 * 숫자/nullable 필드는 서버 전송 시 toCreateRequest / toUpdateRequest 에서 변환.
 * ------------------------------------------------------------------------ */

export interface MemberFormValues {
  loginId: string;
  password: string;
  name: string;
  email: string;
  phone: string;
  zipCode: string;
  roadAddress: string;
  detailAddress: string;
  joinDate: string;
  status: MemberStatus;
  roleId: string;
  departmentId: string;
  positionId: string;
}

export const EMPTY_MEMBER_FORM: MemberFormValues = {
  loginId: '',
  password: '',
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

export function memberDetailToFormValues(d: MemberDetail): MemberFormValues {
  return {
    loginId: d.loginId,
    password: '',
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

export function memberFormToCreateRequest(v: MemberFormValues): MemberCreateRequest {
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

export function memberFormToUpdateRequest(v: MemberFormValues): MemberUpdateRequest {
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
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v;
}
