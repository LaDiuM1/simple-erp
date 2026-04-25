import TextField from '@mui/material/TextField';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import {
  loginIdStatusText,
  renderLoginIdStatusIcon,
} from '@/features/employee/validation/employeeFormValidation';
import type { EmployeeCreateFormState } from '@/features/employee/hooks/useEmployeeCreateForm';
import FormSection from './FormSection';
import { FieldCol2, FieldGrid } from './EmployeeCreateForm.styles';

export default function AccountSection({ form }: { form: EmployeeCreateFormState }) {
  const { values, update, validation, loginIdStatus } = form;

  return (
    <FormSection
      icon={<LockRoundedIcon sx={{ fontSize: 18 }} />}
      title="계정 정보"
      description="로그인에 사용할 ID와 비밀번호를 설정합니다."
    >
      <FieldGrid>
        <TextField
          size="small"
          label="로그인 ID"
          required
          value={values.loginId}
          onChange={(e) => update('loginId', e.target.value)}
          onBlur={validation.onBlur('loginId')}
          error={validation.isInvalid('loginId') || loginIdStatus === 'taken'}
          helperText={validation.errorMessage('loginId') ?? loginIdStatusText(loginIdStatus)}
          placeholder="employee01"
          slotProps={{
            input: { endAdornment: renderLoginIdStatusIcon(loginIdStatus) },
          }}
        />
        <TextField
          size="small"
          type="password"
          label="비밀번호"
          required
          value={values.password}
          onChange={(e) => update('password', e.target.value)}
          onBlur={validation.onBlur('password')}
          error={validation.isInvalid('password')}
          helperText={validation.errorMessage('password') ?? '4자 이상'}
          slotProps={{ htmlInput: { minLength: 4 } }}
        />
        <FieldCol2>
          <TextField
            fullWidth
            size="small"
            type="password"
            label="비밀번호 확인"
            required
            value={values.passwordConfirm}
            onChange={(e) => update('passwordConfirm', e.target.value)}
            onBlur={validation.onBlur('passwordConfirm')}
            error={validation.isInvalid('passwordConfirm')}
            helperText={validation.errorMessage('passwordConfirm')}
          />
        </FieldCol2>
      </FieldGrid>
    </FormSection>
  );
}
