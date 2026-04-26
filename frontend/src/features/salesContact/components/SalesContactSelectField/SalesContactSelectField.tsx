import { useState, type MouseEvent } from 'react';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';
import TextField from '@mui/material/TextField';
import ClearIcon from '@mui/icons-material/Clear';
import SearchIcon from '@mui/icons-material/Search';
import CommonSearchModal from '@/shared/ui/CommonSearchModal';
import { useGetSalesContactsQuery } from '@/features/salesContact/api/salesContactApi';
import {
  salesContactColumn,
  salesContactSearchFilter,
} from '@/features/salesContact/config/salesContactListConfig';
import type { SalesContactSummary } from '@/features/salesContact/types';

interface Props {
  label?: string;
  /** 명부 id (string). 빈 문자열 = 미선택. */
  value: string;
  /** 표시 라벨 — 외부 보유. */
  valueLabel: string;
  onChange: (id: string, name: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
}

/**
 * 영업 명부 검색 모달 트리거 + 선택된 명부명 표시 입력창. EmployeeSelectField / CustomerSelectField 와 동일 패턴.
 */
export default function SalesContactSelectField({
  label = '영업 명부',
  value,
  valueLabel,
  onChange,
  required,
  helperText,
  disabled,
  placeholder,
}: Props) {
  const [open, setOpen] = useState(false);

  const openModal = () => {
    if (!disabled) setOpen(true);
  };

  return (
    <>
      <TextField
        fullWidth
        size="small"
        label={label}
        required={required}
        helperText={helperText}
        disabled={disabled}
        placeholder={placeholder}
        value={valueLabel}
        onClick={openModal}
        slotProps={{
          input: {
            endAdornment: (
              <InputAdornment position="end">
                {!!value && !disabled && (
                  <IconButton
                    size="small"
                    aria-label="선택 해제"
                    onClick={(e) => {
                      e.stopPropagation();
                      onChange('', '');
                    }}
                  >
                    <ClearIcon fontSize="small" />
                  </IconButton>
                )}
                <IconButton
                  size="small"
                  aria-label="명부 검색"
                  onClick={(e) => {
                    e.stopPropagation();
                    openModal();
                  }}
                  disabled={disabled}
                >
                  <SearchIcon fontSize="small" />
                </IconButton>
              </InputAdornment>
            ),
          },
          htmlInput: {
            readOnly: true,
            onMouseDown: (e: MouseEvent<HTMLInputElement>) => e.preventDefault(),
            style: { cursor: 'pointer' },
          },
        }}
      />
      <CommonSearchModal
        open={open}
        onClose={() => setOpen(false)}
        title="영업 명부 검색"
        api={{
          useList: useGetSalesContactsQuery,
          rowKey: (m: SalesContactSummary) => m.id,
          rowLabel: (m: SalesContactSummary) => m.name,
          pageSize: 5,
        }}
        searchFilter={salesContactSearchFilter}
        column={salesContactColumn}
        onSelect={(selected) => {
          const picked = selected[0];
          onChange(picked ? String(picked.id) : '', picked ? picked.label : '');
        }}
        initialSelected={
          value && valueLabel ? [{ id: Number(value), label: valueLabel }] : []
        }
      />
    </>
  );
}
