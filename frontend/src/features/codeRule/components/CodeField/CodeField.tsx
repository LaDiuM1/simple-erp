import { useEffect, useState, type ReactNode } from 'react';
import Box from '@mui/material/Box';
import CircularProgress from '@mui/material/CircularProgress';
import TextField from '@mui/material/TextField';
import CheckCircleRoundedIcon from '@mui/icons-material/CheckCircleRounded';
import ErrorRoundedIcon from '@mui/icons-material/ErrorRounded';
import { useDebouncedValue } from '@/shared/hooks/useDebouncedValue';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { useGetCodeRuleQuery } from '@/features/codeRule/api/codeRuleApi';
import { INPUT_MODE, type CodeRuleTarget } from '@/features/codeRule/types';

export type CodeAvailabilityStatus = 'idle' | 'checking' | 'available' | 'taken';

/** 도메인이 주입하는 코드 중복 검사 hook 시그니처 (RTK Query 호환). */
export type UseCheckCodeAvailability = (
  code: string,
  options?: { skip?: boolean },
) => { data?: { available: boolean }; isFetching: boolean };

const NO_CHECK_HOOK: UseCheckCodeAvailability = () => ({ data: undefined, isFetching: false });

interface CodeFieldProps {
  target: CodeRuleTarget;
  value: string;
  onChange: (next: string) => void;
  /** GenericForm 이 주입 — edit 모드면 변경 불가 + 단순 표시 */
  mode: 'create' | 'edit';
  /** GenericForm 이 disabledOnEdit + mode 로 계산 */
  disabled: boolean;
  label?: string;
  /** 도메인이 주입하는 코드 중복 검사 hook. 미주입 시 검사 생략. */
  useCheckAvailability?: UseCheckCodeAvailability;
}

/**
 * 도메인 등록 폼에서 코드 입력에 사용하는 공용 atom.
 * <p>
 * - edit 모드 / disabled: 단순 readonly 표시
 * - AUTO 모드: 다음 코드를 value 로 표시 + 표준 disabled (클릭 무반응)
 * - AUTO_OR_MANUAL 잠금: 동일하게 다음 코드 표시. 클릭 시 ConfirmModal → 확인 → 수동 입력 활성
 * - MANUAL 또는 잠금 해제: 일반 outlined 입력 + 디바운스 중복 검사 + status icon
 *
 * 모든 시각/포커스 동작 (label floating, hover 하이라이트 등) 은 MUI TextField 표준을 따른다.
 */
export default function CodeField({
  target,
  value,
  onChange,
  mode,
  disabled,
  label = '코드',
  useCheckAvailability,
}: CodeFieldProps) {
  const { data: rule, isLoading } = useGetCodeRuleQuery(target, { skip: mode === 'edit' });

  const [manualUnlocked, setManualUnlocked] = useState(false);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const inputMode = rule?.inputMode;
  const isLocked =
    inputMode === INPUT_MODE.AUTO
    || (inputMode === INPUT_MODE.AUTO_OR_MANUAL && !manualUnlocked);
  const isClickable = inputMode === INPUT_MODE.AUTO_OR_MANUAL && !manualUnlocked;

  // 잠금 상태에서 form value 는 항상 빈 문자열 (BE 가 자동 채번)
  useEffect(() => {
    if (mode === 'create' && isLocked && value !== '') onChange('');
    // 의도적 — onChange/value 미포함. 잠금 활성 시점에만 1회 클리어.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLocked, mode]);

  // edit 모드 / 명시적 disabled — 기존 코드 그대로 표시
  if (mode === 'edit' || disabled) {
    return (
      <TextField
        label={label}
        value={value}
        disabled
        fullWidth
        size="small"
        helperText="코드는 등록 후 변경할 수 없습니다."
      />
    );
  }

  // 규칙 로딩 중
  if (isLoading || !rule) {
    return (
      <TextField
        label={label}
        disabled
        fullWidth
        size="small"
        placeholder="채번 규칙 로딩 중..."
      />
    );
  }

  // 잠금 상태 — 표준 disabled TextField + 다음 코드 value 로 채움. label 정상 floating.
  // 클릭 가능한 경우 (AUTO_OR_MANUAL): 부모 Box 가 클릭 캡처 + TextField 는 pointer-events: none.
  if (isLocked) {
    const helperText = isClickable
      ? '클릭 시 직접 입력으로 전환할 수 있습니다.'
      : '시스템이 자동으로 부여합니다.';

    return (
      <>
        <Box
          onClick={isClickable ? () => setConfirmOpen(true) : undefined}
          sx={{ cursor: isClickable ? 'pointer' : 'default' }}
        >
          <TextField
            label={label}
            value={rule.nextCode ?? ''}
            disabled
            fullWidth
            size="small"
            helperText={helperText}
            sx={{ pointerEvents: 'none' }}
          />
        </Box>
        <ConfirmModal
          isOpen={confirmOpen}
          title="수동 입력 전환"
          message={'자동 코드가 설정되어 있습니다.\n수동으로 입력하시겠습니까?\n코드 채번 규칙에서 규칙 설정이 가능합니다.'}
          confirmLabel="직접 입력"
          cancelLabel="취소"
          onConfirm={() => {
            setManualUnlocked(true);
            setConfirmOpen(false);
          }}
          onCancel={() => setConfirmOpen(false)}
        />
      </>
    );
  }

  // 활성 상태 — 디바운스 중복 검사 + status icon
  return (
    <ActiveCodeField
      label={label}
      value={value}
      onChange={onChange}
      pattern={rule.pattern}
      useCheckAvailability={useCheckAvailability}
    />
  );
}

function ActiveCodeField({
  label,
  value,
  onChange,
  pattern,
  useCheckAvailability,
}: {
  label: string;
  value: string;
  onChange: (next: string) => void;
  pattern: string;
  useCheckAvailability?: UseCheckCodeAvailability;
}) {
  const trimmed = value.trim();
  const debouncedCode = useDebouncedValue(trimmed, 400);
  const skipCheck = debouncedCode === '';

  const checkHook = useCheckAvailability ?? NO_CHECK_HOOK;
  const { data: availability, isFetching } = checkHook(debouncedCode, { skip: skipCheck });

  const status: CodeAvailabilityStatus = !useCheckAvailability || skipCheck
    ? 'idle'
    : trimmed !== debouncedCode || isFetching || !availability
      ? 'checking'
      : availability.available
        ? 'available'
        : 'taken';

  return (
    <TextField
      label={label}
      value={value}
      onChange={(e) => onChange(e.target.value)}
      error={status === 'taken'}
      helperText={statusText(status) ?? `패턴: ${pattern}`}
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

function statusText(status: CodeAvailabilityStatus): string | undefined {
  switch (status) {
    case 'checking':
      return '확인 중...';
    case 'available':
      return '사용 가능한 코드입니다.';
    case 'taken':
      return '이미 사용 중인 코드입니다.';
    case 'idle':
    default:
      return undefined;
  }
}

function renderStatusIcon(status: CodeAvailabilityStatus): ReactNode {
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
