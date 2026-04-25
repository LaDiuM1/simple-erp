import TextField from '@mui/material/TextField';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import type { EmployeeEditFormState } from '@/features/employee/hooks/useEmployeeEditForm';
import { FormSection } from '@/shared/ui/GenericForm';
import { FieldCol2, FieldGrid } from '../employeeForm/employeeForm.styles';

/**
 * 직원 수정 페이지의 계정 섹션.
 * - 로그인 ID 는 식별자로 변경 불가 → disabled.
 * - 비밀번호는 변경 시에만 입력 (비어두면 기존 유지). 확인 필드는 password 가 채워졌을 때만 매칭 검증.
 */
export default function AccountInfoSection({ form }: { form: EmployeeEditFormState }) {
  const { values, update, validation } = form;

  return (
    <FormSection
      icon={<LockRoundedIcon sx={{ fontSize: 18 }} />}
      title="계정 정보"
      description="로그인 ID 는 변경할 수 없습니다. 비밀번호는 변경 시에만 입력하세요."
    >
      <FieldGrid>
        <TextField
          size="small"
          label="로그인 ID"
          value={values.loginId}
          disabled
        />
        <TextField
          size="small"
          type="password"
          label="새 비밀번호"
          placeholder="변경 시 4자 이상 입력"
          value={values.password}
          onChange={(e) => update('password', e.target.value)}
          onBlur={validation.onBlur('password')}
          error={validation.isInvalid('password')}
          helperText={validation.errorMessage('password')}
        />
        <FieldCol2>
          <TextField
            fullWidth
            size="small"
            type="password"
            label="새 비밀번호 확인"
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
