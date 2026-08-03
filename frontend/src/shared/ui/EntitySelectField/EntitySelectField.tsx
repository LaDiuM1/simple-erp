import { useState, type MouseEvent } from 'react';
import { styled } from '@mui/material/styles';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';
import TextField from '@mui/material/TextField';
import ClearIcon from '@mui/icons-material/Clear';
import SearchIcon from '@mui/icons-material/Search';
import CommonSearchModal from '@/shared/ui/CommonSearchModal';
import { SearchTextField } from '@/shared/ui/GenericList';
import type {
  ColumnConfig,
  FilterConfig,
  ListQueryParamsBase,
  QueryState,
} from '@/shared/ui/GenericList';
import type { StateResetKey } from '@/shared/hooks/useResettableState';

/**
 * 도메인별로 한 번 정의하는 EntitySelectField 의 검색 모달 설정.
 * 모달 내부 filter / column / 검색 query 페어를 묶어 wrapper 가 외부로 노출.
 * TFilters 를 검색 hook 과 고정 조건까지 그대로 보존해 wrapper 의 조건 오타를 컴파일 단계에서 막는다.
 */
export interface EntitySelectConfig<TSummary, TFilters extends object> {
  modalTitle: string;
  searchAriaLabel: string;
  useSearchList: (params: TFilters & ListQueryParamsBase) => QueryState<TSummary>;
  rowKey: (row: TSummary) => number;
  rowLabel: (row: TSummary) => string;
  searchFilter: FilterConfig[];
  column: ColumnConfig<TSummary>[];
}

interface Props<TSummary, TFilters extends object> {
  config: EntitySelectConfig<TSummary, TFilters>;
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
  /** 선택 문맥이 강제하는 서버 검색 조건. 검색 필터에서 같은 키를 바꿔도 이 값이 우선한다. */
  fixedQueryParams?: Partial<TFilters>;
  /** 고정 검색 문맥 식별자. 값 변경 시 모달 검색/선택 상태를 초기화한다. */
  scopeKey?: StateResetKey;
  /**
   * 필터바 배치용 dense 톤 — floating label 없이 height 36 의 SearchTextField 톤으로 렌더.
   * 다른 필터 컨트롤(FilterSelect / DateTextField / SearchField)과 박스 크기 / 라벨 스타일을 맞춘다.
   * 미지정(폼) 시 기존 floating-label 폼 필드로 렌더.
   */
  dense?: boolean;
}

/**
 * 도메인 entity 검색 모달 트리거 + 선택된 항목의 라벨 표시 입력창 — 모든 도메인 SelectField 의 공통 base.
 * - 라벨은 호출자 결정 (`valueLabel` prop) — 도메인별 lookup 정책의 차이를 wrapper 에 위임.
 * - 클리어 (X) / 검색 (🔍) adornment + 입력창 클릭으로 모달 오픈.
 * - readOnly input + onMouseDown.preventDefault() 로 input focus 의 "편집 모드" 시각 효과 회피
 *   (키보드 tab focus 는 정상 — a11y 보존).
 */
export default function EntitySelectField<TSummary, TFilters extends object>({
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
  fixedQueryParams,
  scopeKey,
  dense,
}: Props<TSummary, TFilters>) {
  const [open, setOpen] = useState(false);
  const openModal = () => {
    if (!disabled) setOpen(true);
  };

  const trigger = dense ? (
    <DenseSelectTrigger
      size="small"
      variant="outlined"
      placeholder={placeholder ?? label}
      value={valueLabel}
      onClick={openModal}
      slotProps={{
        input: {
          startAdornment: (
            <InputAdornment position="start" sx={{ ml: '-0.125rem' }}>
              <SearchIcon sx={{ fontSize: '1rem', color: 'text.disabled' }} />
            </InputAdornment>
          ),
          endAdornment:
            !!value && !disabled ? (
              <InputAdornment position="end" sx={{ mr: '-0.375rem' }}>
                <IconButton
                  size="small"
                  aria-label="선택 해제"
                  onClick={(e) => {
                    e.stopPropagation();
                    onChange('', '');
                  }}
                  sx={{ p: '2px' }}
                >
                  <ClearIcon sx={{ fontSize: '0.875rem' }} />
                </IconButton>
              </InputAdornment>
            ) : null,
        },
        htmlInput: {
          readOnly: true,
          'aria-label': config.searchAriaLabel,
          onMouseDown: (e: MouseEvent<HTMLInputElement>) => e.preventDefault(),
          style: { cursor: disabled ? 'default' : 'pointer' },
        },
      }}
    />
  ) : (
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
  );

  return (
    <>
      {trigger}
      <CommonSearchModal<TSummary, TFilters>
        open={open}
        onClose={() => setOpen(false)}
        title={config.modalTitle}
        api={{
          useList: config.useSearchList,
          rowKey: config.rowKey,
          rowLabel: config.rowLabel,
        }}
        fixedQueryParams={fixedQueryParams}
        scopeKey={scopeKey}
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

/**
 * dense(필터바) 모드 트리거 — 필터바 검색 input(`SearchTextField`)의 readOnly clickable 변형.
 * height 36 / border 톤을 다른 필터 컨트롤과 공유하고, cursor pointer + placeholder 톤만 얹는다.
 */
const DenseSelectTrigger = styled(SearchTextField)(({ theme }) => ({
  cursor: 'pointer',
  '& .MuiOutlinedInput-root': { cursor: 'pointer' },
  '& .MuiOutlinedInput-input': {
    cursor: 'pointer',
    color: theme.palette.text.primary,
  },
  '& input::placeholder': {
    color: theme.palette.text.disabled,
    opacity: 1,
  },
}));
