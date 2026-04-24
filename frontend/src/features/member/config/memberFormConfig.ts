import { mapIdName } from '@/shared/ui/GenericList';
import type { FieldConfig, FormApiConfig } from '@/shared/ui/GenericForm';
import {
  useCreateMemberMutation,
  useGetMemberQuery,
  useUpdateMemberMutation,
} from '@/features/member/api/memberApi';
import {
  useGetDepartmentsQuery,
  useGetPositionsQuery,
  useGetRolesQuery,
} from '@/features/reference/api/referenceApi';
import {
  EMPTY_MEMBER_FORM,
  MEMBER_STATUS_OPTIONS,
  memberDetailToFormValues,
  memberFormToCreateRequest,
  memberFormToUpdateRequest,
  type MemberCreateRequest,
  type MemberDetail,
  type MemberFormValues,
  type MemberUpdateRequest,
} from '@/features/member/types';

const MENU_CODE = 'MDM_HRM';
const LIST_PATH = '/members';

export const memberFormApi: FormApiConfig<
  MemberFormValues,
  MemberDetail,
  MemberCreateRequest,
  MemberUpdateRequest
> = {
  menuCode: MENU_CODE,
  useGet: useGetMemberQuery,
  useCreate: useCreateMemberMutation,
  useUpdate: useUpdateMemberMutation,
  emptyValues: EMPTY_MEMBER_FORM,
  toValues: memberDetailToFormValues,
  toCreateRequest: memberFormToCreateRequest,
  toUpdateRequest: memberFormToUpdateRequest,
  listPath: LIST_PATH,
};

export const memberFormFields: FieldConfig<MemberFormValues>[] = [
  { type: 'text', key: 'loginId', label: '로그인 ID', required: true, hideOnEdit: true },
  {
    type: 'password',
    key: 'password',
    label: '비밀번호',
    required: true,
    minLength: 8,
    helperText: '8자 이상',
    hideOnEdit: true,
  },
  { type: 'text', key: 'name', label: '이름', required: true },
  { type: 'email', key: 'email', label: '이메일' },
  { type: 'phone', key: 'phone', label: '연락처' },
  { type: 'date', key: 'joinDate', label: '입사일' },
  {
    type: 'select',
    key: 'status',
    label: '상태',
    required: true,
    options: MEMBER_STATUS_OPTIONS,
  },
  {
    type: 'select',
    key: 'roleId',
    label: '권한',
    required: true,
    useOptions: useGetRolesQuery,
    mapOptions: mapIdName,
  },
  {
    type: 'select',
    key: 'departmentId',
    label: '부서',
    useOptions: useGetDepartmentsQuery,
    mapOptions: mapIdName,
  },
  {
    type: 'select',
    key: 'positionId',
    label: '직책',
    useOptions: useGetPositionsQuery,
    mapOptions: mapIdName,
  },
  { type: 'text', key: 'zipCode', label: '우편번호' },
  { type: 'text', key: 'roadAddress', label: '기본 주소', fullWidth: true },
  { type: 'text', key: 'detailAddress', label: '상세 주소', fullWidth: true },
];
