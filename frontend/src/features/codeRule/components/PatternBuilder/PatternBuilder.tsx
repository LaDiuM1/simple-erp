import { forwardRef, useState } from 'react';
import TextField from '@mui/material/TextField';

interface PatternBuilderProps {
  value: string;
  onChange: (next: string) => void;
  onBlur?: () => void;
  error?: boolean;
  helperText?: string;
  disabled?: boolean;
}

const MONOSPACE = 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace';

/**
 * 패턴 입력 + 토큰 카드의 chip 을 받는 drop target.
 * 토큰 추가는 토큰 카드에서. 직접 타이핑으로 literal 구분자 (`-`, `_`, `/` 등) 입력.
 */
const PatternBuilder = forwardRef<HTMLInputElement, PatternBuilderProps>(function PatternBuilder(
  { value, onChange, onBlur, error, helperText, disabled },
  ref,
) {
  const [dragOver, setDragOver] = useState(false);

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    if (disabled) return;
    e.preventDefault();
    e.dataTransfer.dropEffect = 'copy';
    if (!dragOver) setDragOver(true);
  };
  const handleDragLeave = () => setDragOver(false);
  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setDragOver(false);
    if (disabled) return;
    const token = e.dataTransfer.getData('text/plain');
    if (!token) return;
    const inputEl = e.currentTarget.querySelector('input') as HTMLInputElement | null;
    const start = inputEl?.selectionStart ?? value.length;
    const end = inputEl?.selectionEnd ?? value.length;
    const next = value.slice(0, start) + token + value.slice(end);
    onChange(next);
    queueMicrotask(() => {
      if (!inputEl) return;
      inputEl.focus();
      const cursor = start + token.length;
      inputEl.setSelectionRange(cursor, cursor);
    });
  };

  return (
    <TextField
      inputRef={ref}
      variant="outlined"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      onBlur={onBlur}
      error={error}
      helperText={helperText ?? '아래 토큰을 드래그해여 패턴을 추가하거나 직접 입력'}
      disabled={disabled}
      fullWidth
      size="small"
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      onDrop={handleDrop}
      slotProps={{
        input: {
          sx: {
            fontFamily: MONOSPACE,
            fontSize: '0.875rem',
            transition: 'box-shadow 0.15s',
            ...(dragOver && {
              boxShadow: (theme) => `inset 0 0 0 2px ${theme.palette.primary.main}`,
            }),
          },
        },
      }}
    />
  );
});

export default PatternBuilder;
