import PrecisionManufacturingRoundedIcon from '@mui/icons-material/PrecisionManufacturingRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { FieldConfig, FieldOption, FormApiConfig } from '@/shared/ui/GenericForm';
import {
  useCreateProductMutation,
  useGetProductQuery,
  useUpdateProductMutation,
} from '@/features/product/api/productApi';
import { useGetSuppliersQuery } from '@/features/reference/api/referenceApi';
import type { SupplierInfo } from '@/features/reference/types';
import { ACTIVE_FILTER_OPTIONS } from '@/features/supplier/types';
import {
  EMPTY_PRODUCT_FORM,
  PRODUCT_CATEGORY_OPTIONS,
  productDetailToFormValues,
  productFormToRequest,
  type ProductCreateRequest,
  type ProductDetail,
  type ProductFormValues,
  type ProductUpdateRequest,
} from '@/features/product/types';

/** 공급사 드롭다운 옵션 — 영문 표기 (+ 한글 표기) 함께 노출. */
function mapSupplierOptions(data: unknown): FieldOption[] {
  return (data as SupplierInfo[]).map((s) => ({
    value: s.id,
    label: s.nameKo ? `${s.name} (${s.nameKo})` : s.name,
  }));
}

export const productFormFields: FieldConfig<ProductFormValues>[] = [
  {
    key: 'category',
    label: '카테고리',
    type: 'select',
    required: true,
    options: PRODUCT_CATEGORY_OPTIONS,
  },
  {
    key: 'modelName',
    label: '모델명',
    type: 'text',
    required: true,
    maxLength: 100,
    placeholder: '예: HLA-1530',
    helperText: '출력 (kW·ton) 과 옵션은 모델명이 아닌 계약에서 관리합니다.',
  },
  {
    key: 'supplierId',
    label: '공급사',
    type: 'select',
    required: true,
    useOptions: useGetSuppliersQuery,
    mapOptions: mapSupplierOptions,
  },
  {
    key: 'active',
    label: '사용 여부',
    type: 'select',
    options: ACTIVE_FILTER_OPTIONS,
    helperText: '단종 / 취급 중단 모델은 미사용으로 전환합니다.',
  },
  {
    key: 'note',
    label: '비고',
    type: 'text',
    fullWidth: true,
  },
];

export const productFormApi: FormApiConfig<
  ProductFormValues,
  ProductDetail,
  ProductCreateRequest,
  ProductUpdateRequest
> = {
  menuCode: MENU_CODE.PRODUCTS,
  useGet: useGetProductQuery,
  useCreate: useCreateProductMutation,
  useUpdate: useUpdateProductMutation,
  emptyValues: EMPTY_PRODUCT_FORM,
  toValues: productDetailToFormValues,
  toCreateRequest: productFormToRequest,
  toUpdateRequest: productFormToRequest,
  listPath: MENU_PATH[MENU_CODE.PRODUCTS],
  titles: { create: '제품 모델 등록', edit: '제품 모델 수정' },
  confirm: true,
  section: {
    icon: <PrecisionManufacturingRoundedIcon sx={{ fontSize: 18 }} />,
    title: '제품 모델 정보',
    description: '카테고리와 모델명, 공급사를 지정합니다.',
  },
};
