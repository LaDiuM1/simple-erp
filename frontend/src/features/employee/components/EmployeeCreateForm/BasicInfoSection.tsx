import TextField from '@mui/material/TextField';
import PersonRoundedIcon from '@mui/icons-material/PersonRounded';
import type { EmployeeCreateFormState } from '@/features/employee/hooks/useEmployeeCreateForm';
import FormSection from './FormSection';
import { FieldGrid } from './EmployeeCreateForm.styles';

export default function BasicInfoSection({ form }: { form: EmployeeCreateFormState }) {
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
          error={validation.isInvalid('name')}
          helperText={validation.errorMessage('name')}
        />
        <TextField
          size="small"
          type="email"
          label="이메일"
          value={values.email}
          onChange={(e) => update('email', e.target.value)}
          onBlur={validation.onBlur('email')}
          error={validation.isInvalid('email')}
          helperText={validation.errorMessage('email')}
          placeholder="name@example.com"
        />
        <TextField
          size="small"
          type="tel"
          label="연락처"
          value={values.phone}
          onChange={(e) => update('phone', e.target.value)}
          placeholder="010-0000-0000"
        />
        <TextField
          size="small"
          type="date"
          label="입사일"
          value={values.joinDate}
          onChange={(e) => update('joinDate', e.target.value)}
          slotProps={{ inputLabel: { shrink: true } }}
        />
      </FieldGrid>
    </FormSection>
  );
}
