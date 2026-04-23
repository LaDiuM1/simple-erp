import Box from '@mui/material/Box';
import FormControl from '@mui/material/FormControl';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';

export interface FilterOption<V extends string | number> {
  value: V;
  label: string;
}

interface Props<V extends string | number> {
  label: string;
  value: V | null;
  onChange: (value: V | null) => void;
  options: FilterOption<V>[];
  minWidth?: number;
  allLabel?: string;
}

/**
 * SearchFilterBar 내부 filter 슬롯에서 사용하는 공용 드롭다운.
 * 값이 비어있을 때는 label 을, 선택되었을 때는 label + 선택값을 노출한다.
 */
export default function FilterSelect<V extends string | number>({
  label,
  value,
  onChange,
  options,
  minWidth = 110,
  allLabel = '전체',
}: Props<V>) {
  return (
    <FormControl size="small" sx={{ minWidth }}>
      <Select
        value={value == null ? '' : String(value)}
        onChange={(e) => {
          const v = e.target.value;
          if (v === '') {
            onChange(null);
          } else {
            const opt = options.find((o) => String(o.value) === v);
            onChange(opt ? opt.value : null);
          }
        }}
        displayEmpty
        renderValue={(v) => {
          const str = String(v ?? '');
          if (!str) {
            return (
              <Box component="span" sx={{ color: 'text.secondary' }}>
                {label}
              </Box>
            );
          }
          const opt = options.find((o) => String(o.value) === str);
          return (
            <Box component="span" sx={{ display: 'inline-flex', gap: '0.25rem' }}>
              <Box component="span" sx={{ color: 'text.disabled' }}>{label}</Box>
              <Box component="span" sx={{ color: 'text.primary' }}>{opt?.label ?? ''}</Box>
            </Box>
          );
        }}
        sx={(theme) => ({
          height: 36,
          fontSize: '0.8125rem',
          backgroundColor: theme.palette.background.paper,
          '& .MuiSelect-select': {
            py: '0.375rem',
            px: '0.75rem',
            paddingRight: '1.75rem !important',
          },
          '& .MuiOutlinedInput-notchedOutline': {
            borderColor: theme.palette.divider,
          },
          '&:hover .MuiOutlinedInput-notchedOutline': {
            borderColor: theme.palette.text.disabled,
          },
        })}
      >
        <MenuItem value="" sx={{ fontSize: '0.8125rem' }}>{allLabel}</MenuItem>
        {options.map((o) => (
          <MenuItem key={String(o.value)} value={String(o.value)} sx={{ fontSize: '0.8125rem' }}>
            {o.label}
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  );
}
