import PersonRoundedIcon from '@mui/icons-material/PersonRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { FieldConfig, FormApiConfig } from '@/shared/ui/GenericForm';
import {
  useCreateSalesContactMutation,
  useGetSalesContactQuery,
  useUpdateSalesContactMutation,
} from '@/features/salesContact/api/salesContactApi';
import {
  EMPTY_SALES_CONTACT_FORM,
  salesContactDetailToFormValues,
  salesContactFormToCreateRequest,
  salesContactFormToUpdateRequest,
  type SalesContactCreateRequest,
  type SalesContactDetail,
  type SalesContactFormValues,
  type SalesContactUpdateRequest,
} from '@/features/salesContact/types';
import AcquisitionSourceSearchField from '@/features/acquisitionSource/components/AcquisitionSourceSearchField';
import MobilePhoneAvailabilityField from '@/features/salesContact/components/MobilePhoneAvailabilityField';

export const salesContactFormFields: FieldConfig<SalesContactFormValues>[] = [
  { key: 'name', label: '이름', type: 'text', required: true, maxLength: 50 },
  { key: 'nameEn', label: '영문명', type: 'text', maxLength: 50, placeholder: 'Daesung Jung' },
  {
    key: 'mobilePhone',
    label: '휴대폰',
    type: 'custom',
    render: ({ value, onChange, disabled, recordId }) => (
      <MobilePhoneAvailabilityField
        value={typeof value === 'string' ? value : ''}
        onChange={(v) => onChange(v)}
        disabled={disabled}
        excludeId={recordId}
      />
    ),
  },
  { key: 'officePhone', label: '전화번호', type: 'phone' },
  { key: 'email', label: '회사 이메일', type: 'email', placeholder: 'name@company.com' },
  { key: 'personalEmail', label: '개인 이메일', type: 'email' },
  { key: 'metAt', label: '최초 미팅일', type: 'date' },
  {
    key: 'sourceIds',
    label: '컨택 경로',
    type: 'custom',
    fullWidth: true,
    render: ({ value, onChange, disabled }) => (
      <AcquisitionSourceSearchField
        mode="form"
        value={Array.isArray(value) ? (value as number[]) : []}
        onChange={(ids) => onChange(ids)}
        disabled={disabled}
      />
    ),
  },
  { key: 'note', label: '비고', type: 'text', fullWidth: true },
];

export const salesContactFormApi: FormApiConfig<
  SalesContactFormValues,
  SalesContactDetail,
  SalesContactCreateRequest,
  SalesContactUpdateRequest
> = {
  menuCode: MENU_CODE.SALES_CONTACTS,
  useGet: useGetSalesContactQuery,
  useCreate: useCreateSalesContactMutation,
  useUpdate: useUpdateSalesContactMutation,
  emptyValues: EMPTY_SALES_CONTACT_FORM,
  toValues: salesContactDetailToFormValues,
  toCreateRequest: salesContactFormToCreateRequest,
  toUpdateRequest: salesContactFormToUpdateRequest,
  listPath: MENU_PATH[MENU_CODE.SALES_CONTACTS],
  titles: { create: '영업 명부 등록', edit: '영업 명부 수정' },
  confirm: true,
  section: {
    icon: <PersonRoundedIcon sx={{ fontSize: 18 }} />,
    title: '명부 정보',
    description: '외부인 명함 정보 — 이름 / 연락처 / 이메일 / 컨택 경로 등 입력합니다.',
  },
};
