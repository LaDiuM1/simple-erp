export type ProductCategory =
  | 'FLAT'
  | 'H_BEAM'
  | 'PIPE'
  | 'PRESS_BRAKE'
  | 'COMBO'
  | 'DEBURRING'
  | 'EDGE_MACHINE'
  | 'WELDER'
  | 'OSCILLATOR'
  | 'ETC';

export const PRODUCT_CATEGORY_LABELS: Record<ProductCategory, string> = {
  FLAT: '평판 레이저',
  H_BEAM: '형강 레이저',
  PIPE: '파이프 레이저',
  PRESS_BRAKE: '절곡기',
  COMBO: '복합기',
  DEBURRING: '디버링기',
  EDGE_MACHINE: '엣지머신',
  WELDER: '용접기',
  OSCILLATOR: '발진기',
  ETC: '기타',
};

export const PRODUCT_CATEGORY_OPTIONS: { value: ProductCategory; label: string }[] = (
  Object.entries(PRODUCT_CATEGORY_LABELS) as [ProductCategory, string][]
).map(([value, label]) => ({ value, label }));

export interface ProductSummary {
  id: number;
  category: ProductCategory;
  modelName: string;
  supplierId: number;
  supplierName: string | null;
  active: boolean;
}

export interface ProductDetail {
  id: number;
  category: ProductCategory;
  modelName: string;
  supplierId: number;
  supplierName: string | null;
  note: string | null;
  active: boolean;
}

export interface ProductCreateRequest {
  category: ProductCategory;
  modelName: string;
  supplierId: number;
  note: string | null;
  active: boolean;
}

export type ProductUpdateRequest = ProductCreateRequest;

export interface ProductSearchParams {
  modelNameKeyword?: string | null;
  category?: string | null;
  supplierId?: number | null;
  /** 'true' | 'false' — FilterSelect 값은 문자열, BE 가 Boolean 으로 변환 */
  active?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type ProductListFilters = Omit<ProductSearchParams, 'page' | 'size' | 'sort'>;

export interface ProductFormValues {
  /** ProductCategory — select 필드 값은 문자열로 관리, 빈 문자열 = 미선택 */
  category: string;
  modelName: string;
  /** 빈 문자열 = 미선택 */
  supplierId: string;
  note: string;
  /** 'true' | 'false' */
  active: string;
}

export const EMPTY_PRODUCT_FORM: ProductFormValues = {
  category: '',
  modelName: '',
  supplierId: '',
  note: '',
  active: 'true',
};

export function productDetailToFormValues(d: ProductDetail): ProductFormValues {
  return {
    category: d.category,
    modelName: d.modelName,
    supplierId: String(d.supplierId),
    note: d.note ?? '',
    active: String(d.active),
  };
}

export function productFormToRequest(v: ProductFormValues): ProductCreateRequest {
  return {
    category: v.category as ProductCategory,
    modelName: v.modelName.trim(),
    supplierId: Number(v.supplierId),
    note: v.note.trim() === '' ? null : v.note.trim(),
    active: v.active === 'true',
  };
}
