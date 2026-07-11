export interface SupplierSummary {
  id: number;
  name: string;
  nameKo: string | null;
  country: string | null;
  active: boolean;
}

export interface SupplierDetail {
  id: number;
  name: string;
  nameKo: string | null;
  country: string | null;
  note: string | null;
  active: boolean;
}

export interface SupplierCreateRequest {
  name: string;
  nameKo: string | null;
  country: string | null;
  note: string | null;
  active: boolean;
}

export type SupplierUpdateRequest = SupplierCreateRequest;

export interface SupplierSearchParams {
  /** 영문 / 한글 표기 통합 키워드 */
  keyword?: string | null;
  /** 'true' | 'false' — FilterSelect 값은 문자열, BE 가 Boolean 으로 변환 */
  active?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type SupplierListFilters = Omit<SupplierSearchParams, 'page' | 'size' | 'sort'>;

export const ACTIVE_FILTER_OPTIONS: { value: string; label: string }[] = [
  { value: 'true', label: '사용' },
  { value: 'false', label: '미사용' },
];

export interface SupplierFormValues {
  name: string;
  nameKo: string;
  country: string;
  note: string;
  /** 'true' | 'false' — select 필드 값은 문자열로 관리 */
  active: string;
}

export const EMPTY_SUPPLIER_FORM: SupplierFormValues = {
  name: '',
  nameKo: '',
  country: '',
  note: '',
  active: 'true',
};

export function supplierDetailToFormValues(d: SupplierDetail): SupplierFormValues {
  return {
    name: d.name,
    nameKo: d.nameKo ?? '',
    country: d.country ?? '',
    note: d.note ?? '',
    active: String(d.active),
  };
}

export function supplierFormToRequest(v: SupplierFormValues): SupplierCreateRequest {
  return {
    name: v.name.trim(),
    nameKo: v.nameKo.trim() === '' ? null : v.nameKo.trim(),
    country: v.country.trim() === '' ? null : v.country.trim(),
    note: v.note.trim() === '' ? null : v.note.trim(),
    active: v.active === 'true',
  };
}
