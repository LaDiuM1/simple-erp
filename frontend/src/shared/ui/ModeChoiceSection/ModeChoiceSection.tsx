import type { ReactNode } from 'react';
import Box from '@mui/material/Box';
import FormControlLabel from '@mui/material/FormControlLabel';
import Radio from '@mui/material/Radio';
import RadioGroup from '@mui/material/RadioGroup';
import Stack from '@mui/material/Stack';
import Typography from '@mui/material/Typography';

interface Props<M extends string> {
  title: string;
  mode: M;
  onModeChange: (mode: M) => void;
  options: ReadonlyArray<{ value: M; label: string }>;
  children: ReactNode;
}

/**
 * 라디오로 입력 모드를 선택하고 선택된 모드의 입력 UI 만 노출하는 섹션.
 * 옅은 divider 보더 + 작은 라벨 — modal 안에서 inline 으로 자연스럽게 묶이는 톤.
 */
export default function ModeChoiceSection<M extends string>({
  title,
  mode,
  onModeChange,
  options,
  children,
}: Props<M>) {
  return (
    <Box
      sx={{
        border: 1,
        borderColor: 'divider',
        borderRadius: 0,
        px: 2,
        pt: 1.25,
        pb: 2,
      }}
    >
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        alignItems={{ xs: 'flex-start', sm: 'center' }}
        spacing={{ xs: 0.25, sm: 1.5 }}
        sx={{ mb: 1 }}
      >
        <Typography
          variant="caption"
          sx={{ color: 'text.secondary', fontWeight: 500, fontSize: '0.8125rem' }}
        >
          {title}
        </Typography>
        <RadioGroup
          row
          value={mode}
          onChange={(e) => onModeChange(e.target.value as M)}
        >
          {options.map((opt) => (
            <FormControlLabel
              key={opt.value}
              value={opt.value}
              control={<Radio size="small" />}
              label={opt.label}
              sx={{
                mr: 1.5,
                '.MuiFormControlLabel-label': { fontSize: '0.8125rem' },
              }}
            />
          ))}
        </RadioGroup>
      </Stack>
      {children}
    </Box>
  );
}
