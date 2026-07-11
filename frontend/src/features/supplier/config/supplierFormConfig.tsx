import FactoryRoundedIcon from '@mui/icons-material/FactoryRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { FieldConfig, FormApiConfig } from '@/shared/ui/GenericForm';
import {
  useCreateSupplierMutation,
  useGetSupplierQuery,
  useUpdateSupplierMutation,
} from '@/features/supplier/api/supplierApi';
import {
  ACTIVE_FILTER_OPTIONS,
  EMPTY_SUPPLIER_FORM,
  supplierDetailToFormValues,
  supplierFormToRequest,
  type SupplierCreateRequest,
  type SupplierDetail,
  type SupplierFormValues,
  type SupplierUpdateRequest,
} from '@/features/supplier/types';

export const supplierFormFields: FieldConfig<SupplierFormValues>[] = [
  {
    key: 'name',
    label: '공급사명 (영문)',
    type: 'text',
    required: true,
    maxLength: 100,
    placeholder: '예: YAWEI',
    helperText: '계약 / 제품 모델에서 참조하는 표준 표기입니다.',
  },
  {
    key: 'nameKo',
    label: '한글 표기',
    type: 'text',
    maxLength: 100,
    placeholder: '예: 야웨이',
  },
  {
    key: 'country',
    label: '국가',
    type: 'text',
    maxLength: 50,
    placeholder: '예: 중국',
  },
  {
    key: 'active',
    label: '사용 여부',
    type: 'select',
    options: ACTIVE_FILTER_OPTIONS,
    helperText: '거래 중단 공급사는 미사용으로 전환합니다.',
  },
  {
    key: 'note',
    label: '비고',
    type: 'text',
    fullWidth: true,
  },
];

export const supplierFormApi: FormApiConfig<
  SupplierFormValues,
  SupplierDetail,
  SupplierCreateRequest,
  SupplierUpdateRequest
> = {
  menuCode: MENU_CODE.SUPPLIERS,
  useGet: useGetSupplierQuery,
  useCreate: useCreateSupplierMutation,
  useUpdate: useUpdateSupplierMutation,
  emptyValues: EMPTY_SUPPLIER_FORM,
  toValues: supplierDetailToFormValues,
  toCreateRequest: supplierFormToRequest,
  toUpdateRequest: supplierFormToRequest,
  listPath: MENU_PATH[MENU_CODE.SUPPLIERS],
  titles: { create: '공급사 등록', edit: '공급사 수정' },
  confirm: true,
  section: {
    icon: <FactoryRoundedIcon sx={{ fontSize: 18 }} />,
    title: '공급사 정보',
    description: '공급사 표기와 국가, 사용 여부를 관리합니다.',
  },
};
