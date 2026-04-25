import type { ReactNode } from 'react';
import CircularProgress from '@mui/material/CircularProgress';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import ErrorRoundedIcon from '@mui/icons-material/ErrorRounded';
import type { ValidatorMap } from '@/shared/hooks/useFieldValidation';
import type { EmployeeFormValues } from '@/features/employee/types';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export const LOGIN_ID_MIN = 3;

export type LoginIdStatus = 'idle' | 'checking' | 'available' | 'taken';

/** create / edit 양쪽에서 공통으로 검사하는 필드. */
export const employeeBaseValidators: ValidatorMap<EmployeeFormValues> = {
  name: (v) => (v.trim() === '' ? '이름을 입력해주세요.' : null),
  email: (v) =>
    v !== '' && !EMAIL_RE.test(v) ? '이메일 형식이 올바르지 않습니다.' : null,
  roleId: (v) => (v === '' ? '권한을 선택해주세요.' : null),
};

/** create 페이지 전용 — 계정 필드 (loginId / password / passwordConfirm) 추가. */
export const employeeCreateValidators: ValidatorMap<EmployeeFormValues> = {
  ...employeeBaseValidators,
  loginId: (v) => {
    const trimmed = v.trim();
    if (trimmed === '') return '로그인 ID를 입력해주세요.';
    if (trimmed.length < LOGIN_ID_MIN) return `로그인 ID는 ${LOGIN_ID_MIN}자 이상이어야 합니다.`;
    return null;
  },
  password: (v) => (v.length < 4 ? '비밀번호는 4자 이상이어야 합니다.' : null),
  passwordConfirm: (v, all) =>
    v !== all.password ? '비밀번호가 일치하지 않습니다.' : null,
};

export function loginIdStatusText(status: LoginIdStatus): string | undefined {
  switch (status) {
    case 'checking':
      return '확인 중...';
    case 'available':
      return '사용 가능한 ID 입니다.';
    case 'taken':
      return '이미 사용 중인 ID 입니다.';
    case 'idle':
    default:
      return undefined;
  }
}

export function renderLoginIdStatusIcon(status: LoginIdStatus): ReactNode {
  switch (status) {
    case 'checking':
      return <CircularProgress size={14} />;
    case 'available':
      return <CheckCircleRoundedIcon color="success" sx={{ fontSize: 18 }} />;
    case 'taken':
      return <ErrorRoundedIcon color="error" sx={{ fontSize: 18 }} />;
    case 'idle':
    default:
      return null;
  }
}

/** 오늘 날짜를 input[type=date] 가 받는 YYYY-MM-DD 포맷으로 반환. 로컬 시간 기준. */
export function todayIsoDate(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
