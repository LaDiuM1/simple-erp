import BadgeRoundedIcon from '@mui/icons-material/BadgeRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { FieldConfig, FormApiConfig } from '@/shared/ui/GenericForm';
import { useCheckPositionCodeAvailabilityQuery, useCreatePositionMutation, useGetPositionQuery, useUpdatePositionMutation, } from '@/features/position/api/positionApi';
import CodeField from '@/features/codeRule/components/CodeField/CodeField';
import { CODE_RULE_TARGET } from '@/features/codeRule/types';
import { EMPTY_POSITION_FORM, type PositionCreateRequest, type PositionDetail, positionDetailToFormValues, positionFormToCreateRequest, positionFormToUpdateRequest, type PositionFormValues, type PositionUpdateRequest, } from '@/features/position/types';

export const positionFormFields: FieldConfig<PositionFormValues>[] = [
  {
    key: 'code',
    label: '직책 코드',
    type: 'custom',
    fullWidth: true,
    /** 코드는 식별자라 수정 시 변경 불가. 값은 보이도록 유지. */
    disabledOnEdit: true,
    render: ({ value, onChange, mode, disabled }) => (
      <CodeField
        target={CODE_RULE_TARGET.POSITION}
        value={(value as string) ?? ''}
        onChange={(v) => onChange(v)}
        mode={mode}
        disabled={disabled}
        label="직책 코드"
        useCheckAvailability={useCheckPositionCodeAvailabilityQuery}
      />
    ),
  },
  {
    key: 'name',
    label: '직책명',
    type: 'text',
    required: true,
    fullWidth: true,
    maxLength: 100,
  },
  {
    key: 'description',
    label: '설명',
    type: 'text',
    fullWidth: true,
    maxLength: 500,
    helperText: '직책의 역할이나 특이사항을 메모할 수 있습니다.',
  },
];

export const positionFormApi: FormApiConfig<
  PositionFormValues,
  PositionDetail,
  PositionCreateRequest,
  PositionUpdateRequest
> = {
  menuCode: MENU_CODE.POSITIONS,
  useGet: useGetPositionQuery,
  useCreate: useCreatePositionMutation,
  useUpdate: useUpdatePositionMutation,
  emptyValues: EMPTY_POSITION_FORM,
  toValues: positionDetailToFormValues,
  toCreateRequest: positionFormToCreateRequest,
  toUpdateRequest: positionFormToUpdateRequest,
  listPath: MENU_PATH[MENU_CODE.POSITIONS],
  titles: { create: '직책 등록', edit: '직책 수정' },
  /** 직원/부서 등록과 동일하게 submit 직전 confirm modal 1회 노출. */
  confirm: true,
  section: {
    icon: <BadgeRoundedIcon sx={{ fontSize: 18 }} />,
    title: '직책 정보',
    description: '직책 코드와 이름, 설명을 지정합니다.',
  },
};
