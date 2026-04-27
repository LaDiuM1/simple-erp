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

/** 검색 외 필터 드롭다운들을 감싸는 그룹 — flex wrap. */
export const FilterGroup = styled(Box)({
  display: 'flex',
  flexWrap: 'wrap',
  gap: '0.5rem',
});

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

/** 검색 TextField — height 36 + 내부 placeholder/border 톤 조정. */
export const SearchTextField = styled(TextField)(({ theme }) => ({
  [theme.breakpoints.up('md')]: {
    width: 150,
    flex: '0 1 auto',
  },
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
