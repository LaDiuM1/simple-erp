import { useRef } from 'react';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import {
  GroupLabel,
  PaletteGroup,
  PaletteLabel,
  PaletteRow,
  PaletteWrap,
  TokenButton,
} from './PatternBuilder.styles';

interface PatternBuilderProps {
  value: string;
  onChange: (next: string) => void;
  onBlur?: () => void;
  error?: boolean;
  helperText?: string;
  disabled?: boolean;
}

interface TokenDef {
  insert: string;
  label: string;
  hint: string;
}

const TOKEN_GROUPS: { title: string; tokens: TokenDef[] }[] = [
  {
    title: '접두사',
    tokens: [{ insert: '{PREFIX}', label: '{PREFIX}', hint: '규칙의 접두사' }],
  },
  {
    title: '날짜',
    tokens: [
      { insert: '{YYYY}', label: '{YYYY}', hint: '연도 4자리' },
      { insert: '{YY}', label: '{YY}', hint: '연도 2자리' },
      { insert: '{MM}', label: '{MM}', hint: '월 2자리' },
      { insert: '{DD}', label: '{DD}', hint: '일 2자리' },
    ],
  },
  {
    title: '시퀀스',
    tokens: [
      { insert: '{SEQ}', label: '{SEQ}', hint: '기본 자릿수 (Pad)' },
      { insert: '{SEQ:3}', label: '{SEQ:3}', hint: '3자리 (001)' },
      { insert: '{SEQ:4}', label: '{SEQ:4}', hint: '4자리 (0001)' },
      { insert: '{SEQ:5}', label: '{SEQ:5}', hint: '5자리 (00001)' },
      { insert: '{SEQ:6}', label: '{SEQ:6}', hint: '6자리 (000001)' },
    ],
  },
  {
    title: '부모',
    tokens: [{ insert: '{PARENT}', label: '{PARENT}', hint: '부모 코드' }],
  },
];

/**
 * 패턴 입력 + 토큰 팔레트.
 * 토큰 버튼 클릭 시 현재 커서 위치에 토큰을 삽입한다.
 */
export default function PatternBuilder({
  value,
  onChange,
  onBlur,
  error,
  helperText,
  disabled,
}: PatternBuilderProps) {
  const inputRef = useRef<HTMLInputElement | null>(null);

  const insertToken = (token: string) => {
    const input = inputRef.current;
    if (!input || disabled) return;
    const start = input.selectionStart ?? value.length;
    const end = input.selectionEnd ?? value.length;
    const next = value.slice(0, start) + token + value.slice(end);
    onChange(next);
    queueMicrotask(() => {
      input.focus();
      const cursor = start + token.length;
      input.setSelectionRange(cursor, cursor);
    });
  };

  return (
    <Box>
      <TextField
        inputRef={inputRef}
        variant="outlined"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onBlur={onBlur}
        error={error}
        helperText={helperText ?? '예: {PREFIX}-{YYYY}-{SEQ:4}'}
        disabled={disabled}
        fullWidth
        size="small"
        slotProps={{
          input: {
            sx: {
              fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
              fontSize: '0.875rem',
            },
          },
        }}
      />
      <PaletteWrap>
        <PaletteLabel>토큰 삽입</PaletteLabel>
        {TOKEN_GROUPS.map((group) => (
          <PaletteGroup key={group.title}>
            <GroupLabel>{group.title}</GroupLabel>
            <PaletteRow>
              {group.tokens.map((t) => (
                <TokenButton
                  key={t.insert}
                  type="button"
                  size="small"
                  onClick={() => insertToken(t.insert)}
                  disabled={disabled}
                  title={t.hint}
                >
                  {t.label}
                </TokenButton>
              ))}
            </PaletteRow>
          </PaletteGroup>
        ))}
      </PaletteWrap>
    </Box>
  );
}
