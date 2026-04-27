import TextField from '@mui/material/TextField';
import PersonRoundedIcon from '@mui/icons-material/PersonRounded';
import type { EmployeeFormStateBase } from '@/features/employee/hooks/employeeFormState';
import { FormSection } from '@/shared/ui/GenericForm';
import { FieldGrid } from './employeeForm.styles';

interface Props {
  form: EmployeeFormStateBase;
  /** 상세 페이지용 — 모든 입력 컨트롤 disabled. */
  readOnly?: boolean;
}

export default function BasicInfoSection({ form, readOnly = false }: Props) {
  const { values, update, validation } = form;

  return (
    <FormSection
      icon={<PersonRoundedIcon sx={{ fontSize: 18 }} />}
      title="기본 정보"
      description="이름과 연락처 등 직원 식별 정보를 입력합니다."
    >
      <FieldGrid>
        <TextField
          size="small"
          label="이름"
          required
          value={values.name}
          onChange={(e) => update('name', e.target.value)}
          onBlur={validation.onBlur('name')}
          error={!readOnly && validation.isInvalid('name')}
          helperText={!readOnly ? validation.errorMessage('name') : undefined}
          disabled={readOnly}
        />
        <TextField
          size="small"
          type="email"
          label="이메일"
          value={values.email}
          onChange={(e) => update('email', e.target.value)}
          onBlur={validation.onBlur('email')}
          error={!readOnly && validation.isInvalid('email')}
          helperText={!readOnly ? validation.errorMessage('email') : undefined}
          placeholder="name@example.com"
          disabled={readOnly}
        />
        <TextField
          size="small"
          type="tel"
          label="연락처"
          value={values.phone}
          onChange={(e) => update('phone', e.target.value)}
          placeholder="010-0000-0000"
          disabled={readOnly}
        />
        <TextField
          size="small"
          type="date"
          label="입사일"
          value={values.joinDate}
          onChange={(e) => update('joinDate', e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
          disabled={readOnly}
        />
      </FieldGrid>
    </FormSection>
  );
}
