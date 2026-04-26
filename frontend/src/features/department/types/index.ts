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
  /** null/빈 문자열 = 채번 규칙의 inputMode 가 AUTO 또는 AUTO_OR_MANUAL 일 때 시스템이 자동 생성. */
  code: string | null;
  name: string;
  parentId?: number | null;
}

export interface DepartmentUpdateRequest {
  name: string;
  parentId?: number | null;
}

export interface DepartmentSearchParams {
  codeKeyword?: string | null;
  nameKeyword?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type DepartmentListFilters = Omit<DepartmentSearchParams, 'page' | 'size' | 'sort'>;

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
  const trimmedCode = v.code.trim();
  return {
    code: trimmedCode === '' ? null : trimmedCode,
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
