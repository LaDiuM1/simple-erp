export interface DepartmentSummary {
  id: number;
  code: string;
  name: string;
  parentName: string | null;
}

export interface DepartmentDetail {
  id: number;
  code: string;
  name: string;
  parentId: number | null;
  parentName: string | null;
}

export interface DepartmentCreateRequest {
  code: string;
  name: string;
  parentId?: number | null;
}

export interface DepartmentUpdateRequest {
  name: string;
  parentId?: number | null;
}

export interface DepartmentSearchParams {
  keyword?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type DepartmentListFilters = Omit<DepartmentSearchParams, 'page' | 'size' | 'sort'>;

/* --------------------------------------------------------------------------
 * Form (등록/수정 페이지) — MUI 호환을 위해 숫자/nullable 도 string 보관.
 * 서버 전송 시 toCreateRequest / toUpdateRequest 에서 변환.
 * ------------------------------------------------------------------------ */

export interface DepartmentFormValues {
  code: string;
  name: string;
  /** 빈 문자열 = 상위 부서 없음 (최상위) */
  parentId: string;
}

export const EMPTY_DEPARTMENT_FORM: DepartmentFormValues = {
  code: '',
  name: '',
  parentId: '',
};

export function departmentDetailToFormValues(d: DepartmentDetail): DepartmentFormValues {
  return {
    code: d.code,
    name: d.name,
    parentId: d.parentId == null ? '' : String(d.parentId),
  };
}

export function departmentFormToCreateRequest(v: DepartmentFormValues): DepartmentCreateRequest {
  return {
    code: v.code.trim(),
    name: v.name.trim(),
    parentId: v.parentId === '' ? null : Number(v.parentId),
  };
}

export function departmentFormToUpdateRequest(v: DepartmentFormValues): DepartmentUpdateRequest {
  return {
    name: v.name.trim(),
    parentId: v.parentId === '' ? null : Number(v.parentId),
  };
}
