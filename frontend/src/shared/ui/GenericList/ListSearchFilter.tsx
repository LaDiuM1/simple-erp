import { useEffect, useState, type ReactNode } from 'react';
import InputAdornment from '@mui/material/InputAdornment';
import IconButton from '@mui/material/IconButton';
import Tooltip from '@mui/material/Tooltip';
import ClearIcon from '@mui/icons-material/Clear';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import SearchIcon from '@mui/icons-material/Search';
import FilterSelect from '@/shared/ui/atoms/FilterSelect';
import {
  FilterBarContainer,
  FilterGroup,
  ResetButton,
  SearchTextField,
} from './ListSearchFilter.styles';
import type {
  CustomFilterItem,
  FilterConfig,
  FilterOption,
  SearchFilterItem,
  SelectFilterItem,
} from './types';

interface Props {
  searchFilter: FilterConfig[];
  filters: Record<string, unknown>;
  onUpdate: (key: string, value: unknown) => void;
  onReset: () => void;
  debounceMs?: number;
  /** 툴바 우측(초기화 버튼 뒤)에 추가로 렌더할 액션 (예: 엑셀 다운로드) */
  trailing?: ReactNode;
}

/**
 * FilterConfig[] 을 해석해서 검색바 + 필터 드롭다운 묶음을 렌더.
 * type 별로 SearchField / SelectField / CustomField 로 분기한다.
 */
export default function ListSearchFilter({
  searchFilter,
  filters,
  onUpdate,
  onReset,
  debounceMs = 300,
  trailing,
}: Props) {
  const searchItem = searchFilter.find((f): f is SearchFilterItem => f.type === 'search');
  const otherItems = searchFilter.filter((f) => f.type !== 'search');

  return (
    <FilterBarContainer>
      {trailing}

      {otherItems.length > 0 && (
        <FilterGroup>
          {otherItems.map((item) => (
            <FilterItemRenderer
              key={item.key}
              item={item}
              value={filters[item.key]}
              onChange={(v) => onUpdate(item.key, v)}
            />
          ))}
        </FilterGroup>
      )}

      {searchItem && (
        <SearchField
          placeholder={searchItem.placeholder}
          value={toSearchValue(filters[searchItem.key])}
          onChange={(v) => onUpdate(searchItem.key, v === '' ? null : v)}
          debounceMs={debounceMs}
        />
      )}

      <Tooltip title="필터 초기화" arrow>
        <ResetButton onClick={onReset} aria-label="필터 초기화">
          <RestartAltIcon sx={{ fontSize: '1.125rem' }} />
        </ResetButton>
      </Tooltip>
    </FilterBarContainer>
  );
}

function FilterItemRenderer({
  item,
  value,
  onChange,
}: {
  item: Exclude<FilterConfig, SearchFilterItem>;
  value: unknown;
  onChange: (value: unknown) => void;
}): ReactNode {
  if (item.type === 'select') {
    return <SelectField item={item} value={value} onChange={onChange} />;
  }
  return <CustomField item={item} value={value} onChange={onChange} />;
}

function SelectField({
  item,
  value,
  onChange,
}: {
  item: SelectFilterItem;
  value: unknown;
  onChange: (value: unknown) => void;
}) {
  if (item.useOptions) {
    return (
      <DynamicSelect
        useOptions={item.useOptions}
        mapOptions={item.mapOptions}
        fallbackOptions={item.options ?? []}
        label={item.label}
        minWidth={item.minWidth}
        value={value}
        onChange={onChange}
      />
    );
  }
  return (
    <FilterSelect
      label={item.label}
      value={toSelectValue(value)}
      onChange={(v) => onChange(v)}
      options={item.options ?? []}
      minWidth={item.minWidth}
    />
  );
}

function DynamicSelect({
  useOptions,
  mapOptions,
  fallbackOptions,
  label,
  minWidth,
  value,
  onChange,
}: {
  useOptions: () => { data?: unknown };
  mapOptions?: (data: unknown) => FilterOption[];
  fallbackOptions: FilterOption[];
  label: string;
  minWidth?: number;
  value: unknown;
  onChange: (value: unknown) => void;
}) {
  const { data } = useOptions();
  const options: FilterOption[] =
    data != null && mapOptions ? mapOptions(data) : fallbackOptions;

  return (
    <FilterSelect
      label={label}
      value={toSelectValue(value)}
      onChange={(v) => onChange(v)}
      options={options}
      minWidth={minWidth}
    />
  );
}

function CustomField({
  item,
  value,
  onChange,
}: {
  item: CustomFilterItem;
  value: unknown;
  onChange: (value: unknown) => void;
}): ReactNode {
  return <>{item.render({ value, onChange })}</>;
}

function SearchField({
  value,
  onChange,
  placeholder,
  debounceMs,
}: {
  value: string;
  onChange: (v: string) => void;
  placeholder?: string;
  debounceMs: number;
}) {
  const [input, setInput] = useState(value);

  useEffect(() => {
    setInput(value);
  }, [value]);

  useEffect(() => {
    if (input === value) return;
    const id = setTimeout(() => onChange(input), debounceMs);
    return () => clearTimeout(id);
  }, [input, value, onChange, debounceMs]);

  return (
    <SearchTextField
      size="small"
      variant="outlined"
      value={input}
      onChange={(e) => setInput(e.target.value)}
      placeholder={placeholder ?? '검색어를 입력하세요'}
      slotProps={{
        input: {
          startAdornment: (
            <InputAdornment position="start" sx={{ ml: '-0.125rem' }}>
              <SearchIcon sx={{ fontSize: '1rem', color: 'text.disabled' }} />
            </InputAdornment>
          ),
          endAdornment: input ? (
            <InputAdornment position="end" sx={{ mr: '-0.375rem' }}>
              <IconButton
                size="small"
                onClick={() => setInput('')}
                aria-label="검색어 지우기"
                sx={{ p: '2px' }}
              >
                <ClearIcon sx={{ fontSize: '0.875rem' }} />
              </IconButton>
            </InputAdornment>
          ) : null,
        },
      }}
    />
  );
}

function toSelectValue(v: unknown): string | number | null {
  if (v == null) return null;
  if (typeof v === 'string' || typeof v === 'number') return v;
  return null;
}

/** null/기타 → '' (TextField 는 controlled string value 를 요구) */
function toSearchValue(v: unknown): string {
  return typeof v === 'string' ? v : '';
}
