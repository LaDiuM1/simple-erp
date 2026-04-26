import BusinessRoundedIcon from '@mui/icons-material/BusinessRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { FieldConfig, FieldOption, FormApiConfig } from '@/shared/ui/GenericForm';
import {
  useCheckDepartmentCodeAvailabilityQuery,
  useCreateDepartmentMutation,
  useGetDepartmentQuery,
  useUpdateDepartmentMutation,
} from '@/features/department/api/departmentApi';
import CodeField from '@/features/codeRule/components/CodeField/CodeField';
import { CODE_RULE_TARGET } from '@/features/codeRule/types';
import { useGetDepartmentsQuery } from '@/features/reference/api/referenceApi';
import type { DepartmentInfo } from '@/features/reference/types';
import {
  EMPTY_DEPARTMENT_FORM,
  departmentDetailToFormValues,
  departmentFormToCreateRequest,
  departmentFormToUpdateRequest,
  type DepartmentCreateRequest,
  type DepartmentDetail,
  type DepartmentFormValues,
  type DepartmentUpdateRequest,
} from '@/features/department/types';

/** 상위 부서 드롭다운 옵션 — 코드와 이름을 함께 노출. */
function mapParentOptions(data: unknown): FieldOption[] {
  return (data as DepartmentInfo[]).map((d) => ({
    value: d.id,
    label: `${d.name} (${d.code})`,
  }));
}

export const departmentFormFields: FieldConfig<DepartmentFormValues>[] = [
  {
    key: 'code',
    label: '부서 코드',
    type: 'custom',
    fullWidth: true,
    /** 코드는 식별자라 수정 시 변경 불가. 값은 보이도록 유지. */
    disabledOnEdit: true,
    render: ({ value, onChange, mode, disabled }) => (
      <CodeField
        target={CODE_RULE_TARGET.DEPARTMENT}
        value={(value as string) ?? ''}
        onChange={(v) => onChange(v)}
        mode={mode}
        disabled={disabled}
        label="부서 코드"
        useCheckAvailability={useCheckDepartmentCodeAvailabilityQuery}
      />
    ),
  },
  {
    key: 'name',
    label: '부서명',
    type: 'text',
    required: true,
    fullWidth: true,
    maxLength: 100,
  },
  {
    key: 'parentId',
    label: '상위 부서',
    type: 'select',
    fullWidth: true,
    useOptions: useGetDepartmentsQuery,
    mapOptions: mapParentOptions,
    helperText: '선택하지 않으면 최상위 부서로 등록됩니다.',
  },
];

export const departmentFormApi: FormApiConfig<
  DepartmentFormValues,
  DepartmentDetail,
  DepartmentCreateRequest,
  DepartmentUpdateRequest
> = {
  menuCode: MENU_CODE.DEPARTMENTS,
  useGet: useGetDepartmentQuery,
  useCreate: useCreateDepartmentMutation,
  useUpdate: useUpdateDepartmentMutation,
  emptyValues: EMPTY_DEPARTMENT_FORM,
  toValues: departmentDetailToFormValues,
  toCreateRequest: departmentFormToCreateRequest,
  toUpdateRequest: departmentFormToUpdateRequest,
  listPath: MENU_PATH[MENU_CODE.DEPARTMENTS],
  titles: { create: '부서 등록', edit: '부서 수정' },
  /** 직원 등록/수정과 동일하게 submit 직전 confirm modal 1회 노출. */
  confirm: true,
  /** 직원 폼의 섹션 헤더 (icon + title + description) 와 동일한 톤. */
  section: {
    icon: <BusinessRoundedIcon sx={{ fontSize: 18 }} />,
    title: '부서 정보',
    description: '부서 코드와 이름, 상위 부서를 지정합니다.',
  },
};
