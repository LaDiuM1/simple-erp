import { useState, type MouseEvent } from 'react';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';
import TextField from '@mui/material/TextField';
import ClearIcon from '@mui/icons-material/Clear';
import SearchIcon from '@mui/icons-material/Search';
import CommonSearchModal from '@/shared/ui/CommonSearchModal';
import { useGetEmployeesQuery } from '@/features/employee/api/employeeApi';
import {
  employeeColumn,
  employeeSearchFilter,
} from '@/features/employee/config/employeeListConfig';
import type { EmployeeSummary } from '@/features/employee/types';

interface Props {
  label?: string;
  /** 직원 id (string). 빈 문자열 = 미선택. */
  value: string;
  /** 표시 라벨 — 외부에서 보유 (form values 에 employeeId + employeeName 동시 관리). */
  valueLabel: string;
  onChange: (id: string, name: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
}

/**
 * 직원 검색 모달 트리거 + 선택된 직원명 표시 입력창.
 * <p>
 * Department / Role SelectField 와 다르게 employee 는 reference 캐시가 없어 (전체 목록 endpoint 미보유)
 * 표시 라벨을 외부 prop 으로 받는 패턴. 폼 values 가 employeeId + employeeName 둘 다 보유.
 */
export default function EmployeeSelectField({
  label = '직원',
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
                  aria-label="직원 검색"
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
        title="직원 검색"
        api={{
          useList: useGetEmployeesQuery,
          rowKey: (m: EmployeeSummary) => m.id,
          rowLabel: (m: EmployeeSummary) => m.name,
          pageSize: 5,
        }}
        searchFilter={employeeSearchFilter}
        column={employeeColumn}
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
