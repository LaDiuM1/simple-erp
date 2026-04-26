import { useEffect, type ReactNode } from 'react';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import TextField from '@mui/material/TextField';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import ErrorRoundedIcon from '@mui/icons-material/ErrorRounded';
import { useDebouncedValue } from '@/shared/hooks/useDebouncedValue';
import { useCheckRoleCodeAvailabilityQuery } from '@/features/role/api/roleApi';
import { validateRoleCode } from '@/features/role/validation/roleValidation';

type Status = 'idle' | 'invalid' | 'checking' | 'available' | 'taken';

interface Props {
  value: string;
  onChange: (next: string) => void;
  /** 가용성 결과 알림 (부모 폼 상태로 전달) */
  onAvailabilityChange: (available: boolean | null) => void;
  /** validateAll 시 외부에서 주입된 에러 (제출 시) */
  externalError?: string;
}

/**
 * 권한 코드 입력 — 패턴 검증 + 디바운스 가용성 검사 + 상태 아이콘.
 * 등록 모드 전용. 수정 모드는 RoleForm 가 disabled TextField 로 처리.
 */
export default function RoleCodeField({
  value,
  onChange,
  onAvailabilityChange,
  externalError,
}: Props) {
  const trimmed = value.trim();
  const debouncedCode = useDebouncedValue(trimmed, 400);
  const patternError = validateRoleCode(trimmed, 'create');
  const skip = debouncedCode === '' || !!patternError;
  const { data, isFetching } = useCheckRoleCodeAvailabilityQuery(debouncedCode, { skip });

  const status: Status = (() => {
    if (trimmed === '') return 'idle';
    if (patternError) return 'invalid';
    if (trimmed !== debouncedCode || isFetching || !data) return 'checking';
    return data.available ? 'available' : 'taken';
  })();

  const available = status === 'available' ? true : status === 'taken' ? false : null;
  useEffect(() => {
    onAvailabilityChange(available);
  }, [available, onAvailabilityChange]);

  const message =
    externalError
    ?? (status === 'invalid' ? patternError : statusText(status))
    ?? '영문 대문자로 시작 + 영문 대문자/숫자/_ (예: MASTER, MANAGER, STAFF)';

  return (
    <TextField
      label="권한 코드"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      error={!!externalError || status === 'invalid' || status === 'taken'}
      helperText={message}
      fullWidth
      size="small"
      required
      slotProps={{
        input: { endAdornment: renderStatusIcon(status) },
        htmlInput: { maxLength: 50 },
      }}
    />
  );
}

function statusText(status: Status): string | undefined {
  switch (status) {
    case 'checking':
      return '확인 중...';
    case 'available':
      return '사용 가능한 코드입니다.';
    case 'taken':
      return '이미 사용 중인 코드입니다.';
    default:
      return undefined;
  }
}

function renderStatusIcon(status: Status): ReactNode {
  switch (status) {
    case 'checking':
      return <Box sx={{ display: 'flex', alignItems: 'center' }}><CircularProgress size={14} /></Box>;
    case 'available':
      return <CheckCircleRoundedIcon color="success" sx={{ fontSize: 18 }} />;
    case 'taken':
    case 'invalid':
      return <ErrorRoundedIcon color="error" sx={{ fontSize: 18 }} />;
    default:
      return null;
  }
}
