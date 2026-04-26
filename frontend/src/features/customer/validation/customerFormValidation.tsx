import type { ReactNode } from 'react';
import CircularProgress from '@mui/material/CircularProgress';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import ErrorRoundedIcon from '@mui/icons-material/ErrorRounded';
import type { ValidatorMap } from '@/shared/hooks/useFieldValidation';
import type { CustomerFormValues } from '@/features/customer/types';

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const URL_RE = /^https?:\/\/\S+$/i;
const BIZ_REG_NO_RE = /^\d{3}-\d{2}-\d{5}$/;

export type AvailabilityStatus = 'idle' | 'checking' | 'available' | 'taken';

export const customerValidators: ValidatorMap<CustomerFormValues> = {
  name: (v) => (v.trim() === '' ? '고객사명을 입력해주세요.' : null),
  email: (v) =>
    v !== '' && !EMAIL_RE.test(v) ? '이메일 형식이 올바르지 않습니다.' : null,
  website: (v) =>
    v !== '' && !URL_RE.test(v) ? 'http(s):// 로 시작하는 URL을 입력해주세요.' : null,
  bizRegNo: (v) =>
    v !== '' && !BIZ_REG_NO_RE.test(v.trim())
      ? '사업자등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)'
      : null,
};

export function availabilityStatusText(status: AvailabilityStatus, kind: 'code' | 'bizRegNo'): string | undefined {
  const noun = kind === 'code' ? '코드' : '사업자등록번호';
  switch (status) {
    case 'checking':
      return '확인 중...';
    case 'available':
      return `사용 가능한 ${noun}입니다.`;
    case 'taken':
      return `이미 사용 중인 ${noun}입니다.`;
    case 'idle':
    default:
      return undefined;
  }
}

export function renderAvailabilityIcon(status: AvailabilityStatus): ReactNode {
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

export function todayIsoDate(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
