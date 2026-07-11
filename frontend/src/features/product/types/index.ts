export interface ProductCategorySummary {
  id: number;
  name: string;
  sortOrder: number;
  /** 이 카테고리를 참조하는 제품 모델 수 — 삭제 가능 여부 판단용 표시 */
  productCount: number;
}

export interface ProductCategoryCreateRequest {
  name: string;
}

export type ProductCategoryUpdateRequest = ProductCategoryCreateRequest;

export interface ProductCategoryReorderRequest {
  orderedIds: number[];
}

export interface ProductSummary {
  id: number;
  categoryId: number;
  categoryName: string | null;
  modelName: string;
  supplierId: number;
  supplierName: string | null;
  active: boolean;
}

export interface ProductDetail {
  id: number;
  categoryId: number;
  categoryName: string | null;
  modelName: string;
  supplierId: number;
  supplierName: string | null;
  note: string | null;
  active: boolean;
}

export interface ProductCreateRequest {
  categoryId: number;
  modelName: string;
  supplierId: number;
  note: string | null;
  active: boolean;
}

export type ProductUpdateRequest = ProductCreateRequest;

export interface ProductSearchParams {
  modelNameKeyword?: string | null;
  categoryId?: number | null;
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
  /** 빈 문자열 = 미선택 */
  categoryId: string;
  modelName: string;
  /** 빈 문자열 = 미선택 */
  supplierId: string;
  note: string;
  /** 'true' | 'false' */
  active: string;
}

export const EMPTY_PRODUCT_FORM: ProductFormValues = {
  categoryId: '',
  modelName: '',
  supplierId: '',
  note: '',
  active: 'true',
};

export function productDetailToFormValues(d: ProductDetail): ProductFormValues {
  return {
    categoryId: String(d.categoryId),
    modelName: d.modelName,
    supplierId: String(d.supplierId),
    note: d.note ?? '',
    active: String(d.active),
  };
}

export function productFormToRequest(v: ProductFormValues): ProductCreateRequest {
  return {
    categoryId: Number(v.categoryId),
    modelName: v.modelName.trim(),
    supplierId: Number(v.supplierId),
    note: v.note.trim() === '' ? null : v.note.trim(),
    active: v.active === 'true',
  };
}
