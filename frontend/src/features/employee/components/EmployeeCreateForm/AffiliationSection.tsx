import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';
import BadgeRoundedIcon from '@mui/icons-material/BadgeRounded';
import {
  useGetDepartmentsQuery,
  useGetPositionsQuery,
  useGetRolesQuery,
} from '@/features/reference/api/referenceApi';
import type { EmployeeCreateFormState } from '@/features/employee/hooks/useEmployeeCreateForm';
import FormSection from './FormSection';
import { FieldGrid } from './EmployeeCreateForm.styles';

export default function AffiliationSection({ form }: { form: EmployeeCreateFormState }) {
  const { values, update, validation } = form;

  const { data: roles } = useGetRolesQuery();
  const { data: departments } = useGetDepartmentsQuery();
  const { data: positions } = useGetPositionsQuery();

  return (
    <FormSection
      icon={<BadgeRoundedIcon sx={{ fontSize: 18 }} />}
      title="소속 정보"
      description="권한과 부서 / 직책을 지정합니다. 재직 상태는 등록 시 ACTIVE 로 시작됩니다."
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
      </FieldGrid>
    </FormSection>
  );
}
