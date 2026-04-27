import { useMemo, useState, type KeyboardEvent, type MouseEvent } from 'react';
import IconButton from '@mui/material/IconButton';
import InputAdornment from '@mui/material/InputAdornment';
import TextField from '@mui/material/TextField';
import { styled } from '@mui/material/styles';
import ClearIcon from '@mui/icons-material/Clear';
import SearchIcon from '@mui/icons-material/Search';
import { SearchTextField } from '@/shared/ui/GenericList';
import { useGetAcquisitionSourcesQuery } from '@/features/acquisitionSource/api/acquisitionSourceApi';
import type { AcquisitionSourceInfo } from '@/features/acquisitionSource/types';
import AcquisitionSourceSearchModal from './AcquisitionSourceSearchModal';

interface Props {
  /**
   * 'filter': 필터바 키워드 검색 톤 (SearchTextField). 라벨 없음 + 검색 아이콘.
   * 'form': 폼의 일반 TextField 톤 (floating label). 다른 폼 컨트롤과 동일한 디자인.
   */
  mode: 'form' | 'filter';
  value: number[];
  onChange: (ids: number[]) => void;
  label?: string;
  placeholder?: string;
  helperText?: string;
  disabled?: boolean;
}

/**
 * 컨택 경로 검색 트리거 — 폼 / 필터 공통.
 * readOnly 입력으로 키 입력 차단, 클릭 / Enter / Space 로 검색 모달 open.
 * 선택값은 텍스트로만: 0건 → placeholder, 1건 → "이름", 2건+ → "이름 외 N건".
 * 우측 ✕ 로 전체 비우기 (모달 미열림).
 *
 * 디자인 분기:
 *  - filter: SearchTextField (필터바와 동일한 키워드 검색 톤, 검색 아이콘, 라벨 없음).
 *  - form: 일반 TextField (다른 폼 컨트롤과 동일 — floating label + helperText).
 *
 * 두 모드 모두 hover 시 outline + 배경 강조 (호버 효과 동일).
 */
export default function AcquisitionSourceSearchField({
  mode,
  value,
  onChange,
  label = '컨택 경로',
  placeholder = '컨택 경로 추가',
  helperText,
  disabled,
}: Props) {
  const [open, setOpen] = useState(false);
  const { data: allSources = [] } = useGetAcquisitionSourcesQuery();

  const sourceById = useMemo(() => {
    const map = new Map<number, AcquisitionSourceInfo>();
    for (const s of allSources) map.set(s.id, s);
    return map;
  }, [allSources]);

  const summaryText = useMemo(() => {
    if (value.length === 0) return '';
    const first = sourceById.get(value[0]);
    const firstName = first?.name ?? `#${value[0]}`;
    if (value.length === 1) return firstName;
    return `${firstName} 외 ${value.length - 1}건`;
  }, [value, sourceById]);

  const openModal = () => {
    if (!disabled) setOpen(true);
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      openModal();
    }
  };

  const handleClear = (e: MouseEvent<HTMLButtonElement>) => {
    e.stopPropagation();
    onChange([]);
  };

  const handleConfirm = (ids: number[]) => {
    onChange(ids);
    setOpen(false);
  };

  const endAdornment =
    value.length > 0 && !disabled ? (
      <InputAdornment position="end" sx={{ mr: '-0.375rem' }}>
        <IconButton
          size="small"
          onClick={handleClear}
          aria-label="선택 모두 비우기"
          sx={{ p: '2px' }}
        >
          <ClearIcon sx={{ fontSize: '0.875rem' }} />
        </IconButton>
      </InputAdornment>
    ) : null;

  const inputProps = {
    tabIndex: disabled ? -1 : 0,
    'aria-label': label,
    'aria-haspopup': 'dialog' as const,
    style: { cursor: disabled ? 'default' : 'pointer', caretColor: 'transparent' },
  };

  if (mode === 'filter') {
    return (
      <>
        <FilterTrigger
          size="small"
          variant="outlined"
          value={summaryText}
          placeholder={placeholder}
          disabled={disabled}
          onClick={openModal}
          onKeyDown={handleKeyDown}
          slotProps={{
            input: {
              readOnly: true,
              startAdornment: (
                <InputAdornment position="start" sx={{ ml: '-0.125rem' }}>
                  <SearchIcon sx={{ fontSize: '1rem', color: 'text.disabled' }} />
                </InputAdornment>
              ),
              endAdornment,
              inputProps,
            },
          }}
        />
        {open && (
          <AcquisitionSourceSearchModal
            open
            onClose={() => setOpen(false)}
            context="filter"
            initialIds={value}
            onConfirm={handleConfirm}
          />
        )}
      </>
    );
  }

  return (
    <>
      <FormTrigger
        fullWidth
        size="small"
        variant="outlined"
        label={label}
        value={summaryText}
        placeholder={placeholder}
        helperText={helperText}
        disabled={disabled}
        onClick={openModal}
        onKeyDown={handleKeyDown}
        slotProps={{
          inputLabel: { shrink: true },
          input: {
            readOnly: true,
            endAdornment,
            inputProps,
          },
        }}
      />
      {open && (
        <AcquisitionSourceSearchModal
          open
          onClose={() => setOpen(false)}
          context="form"
          initialIds={value}
          onConfirm={handleConfirm}
        />
      )}
    </>
  );
}

/**
 * 필터바 키워드 검색 (`SearchTextField`) 의 readOnly clickable 변형.
 * cursor pointer + hover 시 outline / 배경 강조.
 */
const FilterTrigger = styled(SearchTextField)(({ theme }) => ({
  cursor: 'pointer',
  '& .MuiOutlinedInput-root': {
    cursor: 'pointer',
    transition: 'border-color 0.15s, background-color 0.15s',
  },
  '& .MuiOutlinedInput-input': {
    cursor: 'pointer',
    color: theme.palette.text.primary,
  },
  '& input::placeholder': {
    color: theme.palette.text.disabled,
    opacity: 1,
  },
  '&:hover .MuiOutlinedInput-notchedOutline': {
    borderColor: theme.palette.text.primary,
  },
  '&:hover .MuiOutlinedInput-root': {
    backgroundColor: theme.palette.action.hover,
  },
  '&.Mui-disabled': { cursor: 'default' },
}));

/**
 * 폼의 일반 TextField (floating label) 의 readOnly clickable 변형.
 * 다른 폼 컨트롤 (text / email / phone 등) 과 동일한 시각 톤을 유지하되, hover 효과만 추가.
 */
const FormTrigger = styled(TextField)(({ theme }) => ({
  cursor: 'pointer',
  '& .MuiOutlinedInput-root': {
    cursor: 'pointer',
    transition: 'border-color 0.15s, background-color 0.15s',
  },
  '& .MuiOutlinedInput-input': {
    cursor: 'pointer',
    color: theme.palette.text.primary,
  },
  '& input::placeholder': {
    color: theme.palette.text.disabled,
    opacity: 1,
  },
  '&:hover .MuiOutlinedInput-notchedOutline': {
    borderColor: theme.palette.text.primary,
  },
  '&:hover .MuiOutlinedInput-root': {
    backgroundColor: theme.palette.action.hover,
  },
  '&.Mui-disabled': { cursor: 'default' },
}));
