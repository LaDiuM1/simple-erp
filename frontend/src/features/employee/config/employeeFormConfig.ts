import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { mapIdName } from '@/shared/ui/GenericList';
import type { FieldConfig, FormApiConfig } from '@/shared/ui/GenericForm';
import {
  useCreateEmployeeMutation,
  useGetEmployeeQuery,
  useUpdateEmployeeMutation,
} from '@/features/employee/api/employeeApi';
import {
  useGetDepartmentsQuery,
  useGetPositionsQuery,
  useGetRolesQuery,
} from '@/features/reference/api/referenceApi';
import {
  EMPTY_EMPLOYEE_FORM,
  EMPLOYEE_STATUS_OPTIONS,
  employeeDetailToFormValues,
  employeeFormToCreateRequest,
  employeeFormToUpdateRequest,
  type EmployeeCreateRequest,
  type EmployeeDetail,
  type EmployeeFormValues,
  type EmployeeUpdateRequest,
} from '@/features/employee/types';

export const employeeFormApi: FormApiConfig<
  EmployeeFormValues,
  EmployeeDetail,
  EmployeeCreateRequest,
  EmployeeUpdateRequest
> = {
  menuCode: MENU_CODE.EMPLOYEES,
  useGet: useGetEmployeeQuery,
  useCreate: useCreateEmployeeMutation,
  useUpdate: useUpdateEmployeeMutation,
  emptyValues: EMPTY_EMPLOYEE_FORM,
  toValues: employeeDetailToFormValues,
  toCreateRequest: employeeFormToCreateRequest,
  toUpdateRequest: employeeFormToUpdateRequest,
  listPath: MENU_PATH.EMPLOYEES,
};

export const employeeFormFields: FieldConfig<EmployeeFormValues>[] = [
  { type: 'text', key: 'loginId', label: '로그인 ID', required: true, hideOnEdit: true },
  {
    type: 'password',
    key: 'password',
    label: '비밀번호',
    required: true,
    minLength: 4,
    helperText: '4자 이상',
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
    options: EMPLOYEE_STATUS_OPTIONS,
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
