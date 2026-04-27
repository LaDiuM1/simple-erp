import type { ReactNode } from 'react';
import CircularProgress from '@mui/material/CircularProgress';
import TextField from '@mui/material/TextField';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import ErrorRoundedIcon from '@mui/icons-material/ErrorRounded';
import { useDebouncedValue } from '@/shared/hooks/useDebouncedValue';
import { useCheckSalesContactMobilePhoneAvailabilityQuery } from '@/features/salesContact/api/salesContactApi';

type AvailabilityStatus = 'idle' | 'checking' | 'available' | 'taken';

interface Props {
  value: string;
  onChange: (value: string) => void;
  /** edit 모드일 때 본인 자신은 중복으로 잡지 않도록 제외할 record id. */
  excludeId?: number;
  disabled?: boolean;
  label?: string;
  placeholder?: string;
  required?: boolean;
}

/**
 * 휴대폰 번호 입력 + 디바운스 기반 중복 검사. 입력값이 비어있거나 비활성화 상태면 검사 스킵.
 * helperText / endAdornment 로 상태 표시.
 */
export default function MobilePhoneAvailabilityField({
  value,
  onChange,
  excludeId,
  disabled,
  label = '휴대폰',
  placeholder = '010-0000-0000',
  required,
}: Props) {
  const trimmed = value.trim();
  const debounced = useDebouncedValue(trimmed, 400);
  const skip = disabled === true || debounced === '';
  const { data, isFetching } = useCheckSalesContactMobilePhoneAvailabilityQuery(
    { mobilePhone: debounced, ...(excludeId == null ? {} : { excludeId }) },
    { skip },
  );

  const status: AvailabilityStatus = skip
    ? 'idle'
    : trimmed !== debounced || isFetching || !data
      ? 'checking'
      : data.available
        ? 'available'
        : 'taken';

  return (
    <TextField
      fullWidth
      size="small"
      type="tel"
      label={label}
      value={value}
      onChange={(e) => onChange(e.target.value)}
      placeholder={placeholder}
      required={required}
      disabled={disabled}
      error={status === 'taken'}
      helperText={statusText(status)}
      slotProps={{
        input: { endAdornment: disabled ? undefined : renderIcon(status) },
      }}
    />
  );
}

function statusText(status: AvailabilityStatus): string | undefined {
  switch (status) {
    case 'checking':
      return '확인 중...';
    case 'available':
      return '사용 가능한 휴대폰 번호입니다.';
    case 'taken':
      return '이미 등록된 휴대폰 번호입니다.';
    case 'idle':
    default:
      return undefined;
  }
}

function renderIcon(status: AvailabilityStatus): ReactNode {
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
