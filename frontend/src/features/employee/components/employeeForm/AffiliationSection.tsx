import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import BadgeRoundedIcon from '@mui/icons-material/BadgeRounded';
import {
  useGetDepartmentsQuery,
  useGetPositionsQuery,
  useGetRolesQuery,
} from '@/features/reference/api/referenceApi';
import {
  EMPLOYEE_STATUS_OPTIONS,
  type EmployeeStatus,
} from '@/features/employee/types';
import type { EmployeeFormStateBase } from '@/features/employee/hooks/employeeFormState';
import FormSection from './FormSection';
import { FieldGrid } from './employeeForm.styles';

interface Props {
  form: EmployeeFormStateBase;
  /** 재직 상태 셀렉트 노출 여부. create 페이지에서는 ACTIVE 고정이므로 숨김. */
  showStatus?: boolean;
}

export default function AffiliationSection({ form, showStatus = false }: Props) {
  const { values, update, validation } = form;

  const { data: roles } = useGetRolesQuery();
  const { data: departments } = useGetDepartmentsQuery();
  const { data: positions } = useGetPositionsQuery();

  const description = showStatus
    ? '권한과 부서 / 직책 / 재직 상태를 지정합니다.'
    : '권한과 부서 / 직책을 지정합니다. 재직 상태는 등록 시 ACTIVE 로 시작됩니다.';

  return (
    <FormSection
      icon={<BadgeRoundedIcon sx={{ fontSize: 18 }} />}
      title="소속 정보"
      description={description}
    >
      <FieldGrid>
        <TextField
          select
          size="small"
          label="권한"
          required
          value={values.roleId}
          onChange={(e) => update('roleId', e.target.value)}
          onBlur={validation.onBlur('roleId')}
          error={validation.isInvalid('roleId')}
          helperText={validation.errorMessage('roleId')}
        >
          {(roles ?? []).map((r) => (
            <MenuItem key={r.id} value={String(r.id)}>
              {r.name}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          select
          size="small"
          label="부서"
          value={values.departmentId}
          onChange={(e) => update('departmentId', e.target.value)}
        >
          <MenuItem value="">-</MenuItem>
          {(departments ?? []).map((d) => (
            <MenuItem key={d.id} value={String(d.id)}>
              {d.name}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          select
          size="small"
          label="직책"
          value={values.positionId}
          onChange={(e) => update('positionId', e.target.value)}
        >
          <MenuItem value="">-</MenuItem>
          {(positions ?? []).map((p) => (
            <MenuItem key={p.id} value={String(p.id)}>
              {p.name}
            </MenuItem>
          ))}
        </TextField>
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
