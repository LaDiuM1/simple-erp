import { useState, type MouseEvent } from 'react';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';
import TextField from '@mui/material/TextField';
import ClearIcon from '@mui/icons-material/Clear';
import SearchIcon from '@mui/icons-material/Search';
import CommonSearchModal from '@/shared/ui/CommonSearchModal';
import type {
  ColumnConfig,
  FilterConfig,
  QueryState,
} from '@/shared/ui/GenericList';

/**
 * 도메인별로 한 번 정의하는 EntitySelectField 의 검색 모달 설정.
 * 모달 내부 filter / column / 검색 query 페어를 묶어 wrapper 가 외부로 노출.
 * 도메인마다 filter param 타입이 다르므로 TFilters 는 `any` 로 erase — 도메인 wrapper 가
 * config 와 그 wrapper 호출자의 짝을 보장해 런타임 안전.
 */
export interface EntitySelectConfig<TSummary> {
  modalTitle: string;
  searchAriaLabel: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  useSearchList: (params: any) => QueryState<TSummary>;
  rowKey: (row: TSummary) => number;
  rowLabel: (row: TSummary) => string;
  searchFilter: FilterConfig[];
  column: ColumnConfig<TSummary>[];
}

interface Props<TSummary> {
  config: EntitySelectConfig<TSummary>;
  label: string;
  /** 선택된 entity id (string). 빈 문자열 = 미선택. */
  value: string;
  /**
   * 선택된 entity 의 표시 라벨. 호출자가 결정 / 보유:
   *  - reference 캐시가 있는 도메인 (Department / Position / Role) → 도메인 wrapper 가 hook 으로 lookup
   *  - reference 캐시가 없는 도메인 (Employee / Customer / SalesContact) → 폼이 외부로 보유
   */
  valueLabel: string;
  /** 행 선택 / 해제 시 호출 — id 와 표시명을 함께 전달. 미선택은 ('', ''). */
  onChange: (id: string, name: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
  /** 결과 목록에서 제외할 id (예: 자기 자신을 상위로 못 고르도록). */
  excludeId?: number;
}

/**
 * 도메인 entity 검색 모달 트리거 + 선택된 항목의 라벨 표시 입력창 — 모든 도메인 SelectField 의 공통 base.
 * - 라벨은 호출자 결정 (`valueLabel` prop) — 도메인별 lookup 정책의 차이를 wrapper 에 위임.
 * - 클리어 (X) / 검색 (🔍) adornment + 입력창 클릭으로 모달 오픈.
 * - readOnly input + onMouseDown.preventDefault() 로 input focus 의 "편집 모드" 시각 효과 회피
 *   (키보드 tab focus 는 정상 — a11y 보존).
 */
export default function EntitySelectField<TSummary>({
  config,
  label,
  value,
  valueLabel,
  onChange,
  required,
  helperText,
  disabled,
  placeholder,
  excludeId,
}: Props<TSummary>) {
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
                  aria-label={config.searchAriaLabel}
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
            style: { cursor: disabled ? 'default' : 'pointer' },
          },
        }}
      />
      <CommonSearchModal<TSummary, never>
        open={open}
        onClose={() => setOpen(false)}
        title={config.modalTitle}
        api={{
          useList: config.useSearchList,
          rowKey: config.rowKey,
          rowLabel: config.rowLabel,
        }}
        searchFilter={config.searchFilter}
        column={config.column}
        onSelect={(selected) => {
          const picked = selected[0];
          onChange(picked ? String(picked.id) : '', picked ? picked.label : '');
        }}
        initialSelected={
          value && valueLabel ? [{ id: Number(value), label: valueLabel }] : []
        }
        excludeIds={excludeId != null ? [excludeId] : undefined}
      />
    </>
  );
}
