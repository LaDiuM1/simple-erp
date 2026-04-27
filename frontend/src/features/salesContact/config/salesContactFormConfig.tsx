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

export const salesContactFormFields: FieldConfig<SalesContactFormValues>[] = [
  { key: 'name', label: '이름', type: 'text', required: true, maxLength: 50 },
  { key: 'nameEn', label: '영문명', type: 'text', maxLength: 50, placeholder: 'Daesung Jung' },
  { key: 'mobilePhone', label: '휴대폰', type: 'phone', placeholder: '010-0000-0000' },
  { key: 'officePhone', label: '전화번호', type: 'phone' },
  { key: 'email', label: '회사 이메일', type: 'email', placeholder: 'name@company.com' },
  { key: 'personalEmail', label: '개인 이메일', type: 'email' },
  { key: 'metAt', label: '최초 미팅일', type: 'date' },
  { key: 'metVia', label: '만난 경로', type: 'text', maxLength: 100, placeholder: '전시회 / 소개 / 미팅 등' },
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
    description: '외부인 명함 정보 — 이름 / 연락처 / 이메일 / 만난 경로 등 입력합니다.',
  },
};
