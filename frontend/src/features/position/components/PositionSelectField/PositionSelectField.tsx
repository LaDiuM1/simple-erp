import { useState, type MouseEvent } from 'react';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';
import TextField from '@mui/material/TextField';
import ClearIcon from '@mui/icons-material/Clear';
import SearchIcon from '@mui/icons-material/Search';
import CommonSearchModal from '@/shared/ui/CommonSearchModal';
import { useGetPositionsSummaryQuery } from '@/features/position/api/positionApi';
import {
  positionColumn,
  positionSearchFilter,
} from '@/features/position/config/positionListConfig';
import type { PositionSummary } from '@/features/position/types';
import { useGetPositionsQuery } from '@/features/reference/api/referenceApi';

interface Props {
  /** TextField label. */
  label?: string;
  /** 직책 id (string). 빈 문자열 = 미선택. */
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
  /** 결과 목록에서 제외할 직책 id. */
  excludeId?: number;
}

/**
 * 직책 검색 모달 트리거 + 선택된 직책명 표시 입력창.
 * - value: id (string) — 폼 값으로 그대로 사용 가능
 * - 라벨 표시: 캐시된 useGetPositionsQuery (전체 직책 목록) 에서 lookup
 * - 클리어 (X) 와 검색 (🔍) 두 adornment + 입력창 클릭으로 모달 오픈
 */
export default function PositionSelectField({
  label = '직책',
  value,
  onChange,
  required,
  helperText,
  disabled,
  placeholder,
  excludeId,
}: Props) {
  const [open, setOpen] = useState(false);

  // 선택된 직책의 표시 라벨 — 모달과 별개로 캐시된 reference 쿼리에서 lookup.
  const { data: positions = [] } = useGetPositionsQuery();
  const selectedLabel = positions.find((p) => String(p.id) === value)?.name ?? '';

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
                  aria-label="직책 검색"
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
            /**
             * 마우스 클릭으로는 input 에 focus 가 가지 않게 default 를 prevent.
             * 모달을 여는 트리거 필드라 input focus 의 시각 효과 (글로벌 focus ring) 가
             * "편집 모드 진입" 처럼 오해를 부르기 때문. 키보드 tab focus 는 정상 동작 (a11y 보존).
             */
            onMouseDown: (e: MouseEvent<HTMLInputElement>) => e.preventDefault(),
            style: { cursor: 'pointer' },
          },
        }}
      />
      <CommonSearchModal
        open={open}
        onClose={() => setOpen(false)}
        title="직책 검색"
        api={{
          useList: useGetPositionsSummaryQuery,
          rowKey: (m: PositionSummary) => m.id,
          rowLabel: (m: PositionSummary) => m.name,
          pageSize: 5,
        }}
        searchFilter={positionSearchFilter}
        column={positionColumn}
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
