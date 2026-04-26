import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import BadgeRoundedIcon from '@mui/icons-material/BadgeRounded';
import DepartmentSelectField from '@/features/department/components/DepartmentSelectField';
import PositionSelectField from '@/features/position/components/PositionSelectField';
import RoleSelectField from '@/features/role/components/RoleSelectField';
import {
  EMPLOYEE_STATUS_OPTIONS,
  type EmployeeStatus,
} from '@/features/employee/types';
import type { EmployeeFormStateBase } from '@/features/employee/hooks/employeeFormState';
import { FormSection } from '@/shared/ui/GenericForm';
import { FieldGrid } from './employeeForm.styles';

interface Props {
  form: EmployeeFormStateBase;
  /** 재직 상태 셀렉트 노출 여부. create 페이지에서는 ACTIVE 고정이므로 숨김. */
  showStatus?: boolean;
}

export default function AffiliationSection({ form, showStatus = false }: Props) {
  const { values, update, validation } = form;

  const description = showStatus
    ? '권한과 부서 / 직책 / 재직 상태를 지정합니다.'
    : '권한과 부서 / 직책을 지정합니다.';

  return (
    <FormSection
      icon={<BadgeRoundedIcon sx={{ fontSize: 18 }} />}
      title="소속 정보"
      description={description}
    >
      <FieldGrid>
        <RoleSelectField
          value={values.roleId}
          onChange={(v) => update('roleId', v)}
          required
          helperText={validation.errorMessage('roleId')}
        />
        <DepartmentSelectField
          value={values.departmentId}
          onChange={(v) => update('departmentId', v)}
        />
        <PositionSelectField
          value={values.positionId}
          onChange={(v) => update('positionId', v)}
        />
        {showStatus && (
          <TextField
            select
            size="small"
            label="재직 상태"
            required
            value={values.status}
            onChange={(e) => update('status', e.target.value as EmployeeStatus)}
          >
            {EMPLOYEE_STATUS_OPTIONS.map((opt) => (
              <MenuItem key={opt.value} value={opt.value}>
                {opt.label}
              </MenuItem>
            ))}
          </TextField>
        )}
      </FieldGrid>
    </FormSection>
  );
}
