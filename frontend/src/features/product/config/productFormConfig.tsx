import PrecisionManufacturingRoundedIcon from '@mui/icons-material/PrecisionManufacturingRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { FieldConfig, FieldOption, FormApiConfig } from '@/shared/ui/GenericForm';
import {
  useCreateProductMutation,
  useGetProductCategoriesQuery,
  useGetProductQuery,
  useUpdateProductMutation,
} from '@/features/product/api/productApi';
import ProductSupplierField from '@/features/product/components/ProductSupplierField/ProductSupplierField';
import { ACTIVE_FILTER_OPTIONS } from '@/features/supplier/types';
import {
  EMPTY_PRODUCT_FORM,
  productDetailToFormValues,
  productFormToRequest,
  type ProductCategorySummary,
  type ProductCreateRequest,
  type ProductDetail,
  type ProductFormValues,
  type ProductUpdateRequest,
} from '@/features/product/types';

/** 카테고리 드롭다운 옵션 — 카테고리 관리 화면의 노출 순서 그대로. */
function mapCategoryOptions(data: unknown): FieldOption[] {
  return (data as ProductCategorySummary[]).map((c) => ({ value: c.id, label: c.name }));
}

export const productFormFields: FieldConfig<ProductFormValues>[] = [
  {
    key: 'categoryId',
    label: '카테고리',
    type: 'select',
    required: true,
    useOptions: useGetProductCategoriesQuery,
    mapOptions: mapCategoryOptions,
    helperText: '분류 추가 / 변경은 목록의 카테고리 관리에서 할 수 있습니다.',
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
    type: 'custom',
    required: true,
    render: ({ value, onChange, mode, disabled }) => (
      <ProductSupplierField
        value={value}
        onChange={onChange}
        mode={mode}
        disabled={disabled}
      />
    ),
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
