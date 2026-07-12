import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import TextField from '@mui/material/TextField';

/** 필터바 컨테이너 — 모바일은 세로 stretch, md+ 는 가로 우측 정렬 (폭 부족 시 wrap 으로 자동 줄바꿈). */
export const FilterBarContainer = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'stretch',
  gap: '0.5rem',
  [theme.breakpoints.up('md')]: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'flex-end',
  },
}));

/**
 * 필터 컨트롤 1개가 md+ 에서 차지하는 고정 폭 (px).
 * select / date / search / entity 모든 필터가 이 폭으로 통일 — 종류가 달라도 박스 크기가 어긋나지 않는다.
 */
export const FILTER_CONTROL_WIDTH = 200;

/**
 * 필터 컨트롤 1개를 감싸는 폭 슬롯 — 컨트롤 종류(select/date/search/custom)와 무관하게 동일 폭을 강제.
 * 내부 컨트롤은 폭 100% 로 슬롯을 채우므로, 각 컨트롤이 제각각 들고 있던 width/minWidth 는 슬롯이 덮어쓴다.
 * 모바일(<md): 컨테이너 폭 전체로 stretch (FilterBarContainer 가 column stretch).
 */
export const FilterSlot = styled(Box)(({ theme }) => ({
  width: '100%',
  '& > *': { width: '100%' },
  [theme.breakpoints.up('md')]: {
    width: FILTER_CONTROL_WIDTH,
    flexShrink: 0,
  },
}));

/** 필터 초기화(🔄) 버튼. */
export const ResetButton = styled(IconButton)(({ theme }) => ({
  width: 32,
  height: 32,
  borderRadius: '6px',
  color: theme.palette.text.secondary,
  '&:hover': {
    color: theme.palette.text.primary,
    backgroundColor: 'rgba(15, 23, 42, 0.05)',
  },
  [theme.breakpoints.up('md')]: {
    alignSelf: 'center',
  },
}));

/** 날짜 필터 TextField — FilterSelect 와 동일한 height 36 / border 톤. */
export const DateTextField = styled(TextField)(({ theme }) => ({
  '& .MuiOutlinedInput-root': {
    height: 36,
    fontSize: '0.8125rem',
    backgroundColor: theme.palette.background.paper,
    paddingLeft: '0.75rem',
  },
  '& .MuiOutlinedInput-input': {
    paddingTop: '0.375rem',
    paddingBottom: '0.375rem',
  },
  '& .MuiOutlinedInput-notchedOutline': { borderColor: theme.palette.divider },
  '&:hover .MuiOutlinedInput-notchedOutline': {
    borderColor: theme.palette.text.disabled,
  },
}));

/** 검색 TextField — height 36 + 내부 placeholder/border 톤 조정. 폭은 FilterSlot 이 통일. */
export const SearchTextField = styled(TextField)(({ theme }) => ({
  '& .MuiOutlinedInput-root': {
    height: 36,
    fontSize: '0.8125rem',
    backgroundColor: theme.palette.background.paper,
    paddingLeft: '0.625rem',
    paddingRight: '0.5rem',
  },
  '& .MuiOutlinedInput-input': {
    paddingTop: '0.375rem',
    paddingBottom: '0.375rem',
    '&::placeholder': { fontSize: '0.8125rem' },
  },
  '& .MuiOutlinedInput-notchedOutline': { borderColor: theme.palette.divider },
  '&:hover .MuiOutlinedInput-notchedOutline': {
    borderColor: theme.palette.text.disabled,
  },
}));
