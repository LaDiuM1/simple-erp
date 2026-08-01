import type { ReactNode } from 'react';
import TextField from '@mui/material/TextField';
import { CONTENT_TEXT_MAX_LENGTH } from '@/shared/validation/contentText';

interface Props {
  label: string;
  value: string;
  onChange: (value: string) => void;
  onBlur?: () => void;
  required?: boolean;
  minRows?: number;
  error?: boolean;
  helperText?: ReactNode;
}

/** 게시글·결재 본문의 문자 상한과 즉시 길이 피드백을 한곳에서 적용하는 공용 입력 필드. */
export default function ContentTextField({
  label,
  value,
  onChange,
  onBlur,
  required,
  minRows = 8,
  error = false,
  helperText,
}: Props) {
  const lengthExceeded = value.length > CONTENT_TEXT_MAX_LENGTH;
  const lengthText = `${value.length.toLocaleString('ko-KR')} / ${CONTENT_TEXT_MAX_LENGTH.toLocaleString('ko-KR')}자`;

  return (
    <TextField
      size="small"
      label={label}
      required={required}
      multiline
      minRows={minRows}
      value={value}
      onChange={(event) => onChange(event.target.value)}
      onBlur={onBlur}
      error={error || lengthExceeded}
      helperText={
        lengthExceeded
          ? `${label}은 ${CONTENT_TEXT_MAX_LENGTH.toLocaleString('ko-KR')}자 이하로 입력해주세요.`
          : (helperText ?? lengthText)
      }
      slotProps={{ htmlInput: { maxLength: CONTENT_TEXT_MAX_LENGTH } }}
    />
  );
}
