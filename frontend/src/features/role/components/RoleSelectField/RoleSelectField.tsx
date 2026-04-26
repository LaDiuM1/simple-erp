import { useState, type MouseEvent } from 'react';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';
import TextField from '@mui/material/TextField';
import ClearIcon from '@mui/icons-material/Clear';
import SearchIcon from '@mui/icons-material/Search';
import CommonSearchModal from '@/shared/ui/CommonSearchModal';
import { useGetRolesSummaryQuery } from '@/features/role/api/roleApi';
import {
  roleColumn,
  roleSearchFilter,
} from '@/features/role/config/roleListConfig';
import type { RoleSummary } from '@/features/role/types';
import { useGetRolesQuery } from '@/features/reference/api/referenceApi';

interface Props {
  /** TextField label. */
  label?: string;
  /** 권한 id (string). 빈 문자열 = 미선택. */
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
  /** 결과 목록에서 제외할 권한 id. */
  excludeId?: number;
}

/**
 * 권한 검색 모달 트리거 + 선택된 권한명 표시 입력창.
 * - value: id (string) — 폼 값으로 그대로 사용 가능
 * - 라벨 표시: 캐시된 useGetRolesQuery (전체 권한 목록) 에서 lookup
 * - 클리어 (X) 와 검색 (🔍) 두 adornment + 입력창 클릭으로 모달 오픈
 */
export default function RoleSelectField({
  label = '권한',
  value,
  onChange,
  required,
  helperText,
  disabled,
  placeholder,
  excludeId,
}: Props) {
  const [open, setOpen] = useState(false);

  // 선택된 권한의 표시 라벨 — 모달과 별개로 캐시된 reference 쿼리에서 lookup.
  const { data: roles = [] } = useGetRolesQuery();
  const selectedLabel = roles.find((r) => String(r.id) === value)?.name ?? '';

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
        value={selectedLabel}
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
                      onChange('');
                    }}
                  >
                    <ClearIcon fontSize="small" />
                  </IconButton>
                )}
                <IconButton
                  size="small"
                  aria-label="권한 검색"
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
        title="권한 검색"
        api={{
          useList: useGetRolesSummaryQuery,
          rowKey: (m: RoleSummary) => m.id,
          rowLabel: (m: RoleSummary) => m.name,
          pageSize: 5,
        }}
        searchFilter={roleSearchFilter}
        column={roleColumn}
        onSelect={(selected) => {
          const picked = selected[0];
          onChange(picked ? String(picked.id) : '');
        }}
        initialSelected={
          value && selectedLabel ? [{ id: Number(value), label: selectedLabel }] : []
        }
        excludeIds={excludeId != null ? [excludeId] : undefined}
      />
    </>
  );
}
