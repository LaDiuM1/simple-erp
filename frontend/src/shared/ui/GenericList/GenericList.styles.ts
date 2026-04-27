import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';

/** 페이지 MainContent 패딩을 음수 마진으로 탈출하는 full-bleed 외곽 컨테이너. */
export const ListRoot = styled(Box)(({ theme }) => ({
  minHeight: 0,
  margin: '-1rem',
  [theme.breakpoints.up('sm')]: {
    margin: '-2rem',
  },
  [theme.breakpoints.up('md')]: {
    height: 'calc(100% + 4rem)',
    display: 'flex',
    flexDirection: 'column',
  },
}));

/** ListRoot 안쪽의 카드 surface (필터바 / 테이블 / 페이지네이션을 담음). */
export const ListSurface = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  borderRadius: 0,
  overflow: 'hidden',
  minHeight: 0,
  [theme.breakpoints.up('md')]: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
  },
}));

/** ListSurface 상단의 검색 / 필터 영역 — 아래 테이블과 구분선으로 분리. */
export const FilterBarArea = styled(Box)(({ theme }) => ({
  padding: '0.875rem 1rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  flexShrink: 0,
}));

/**
 * 필터바 좌측 액션 그룹 — 일괄 삭제 / 엑셀 다운로드 등의 버튼 묶음.
 * md+ 에서 marginRight: auto 로 나머지 필터/검색/리셋을 우측으로 밀어낸다.
 */
export const FilterBarTrailing = styled(Box)(({ theme }) => ({
  display: 'flex',
  gap: '0.5rem',
  alignItems: 'center',
  [theme.breakpoints.up('md')]: {
    marginRight: 'auto',
  },
}));

/**
 * 필터바 **좌측**에 노출되는 엑셀 다운로드 버튼.
 * 높이는 FilterSelect / SearchField 와 동일한 36px.
 * md+ 에서 margin-right: auto 로 나머지 필터/검색/리셋을 우측으로 밀어낸다.
 */
export const ExcelDownloadButton = styled(Button)(({ theme }) => ({
  height: 36,
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  borderColor: theme.palette.divider,
  borderRadius: '6px',
  textTransform: 'none',
  paddingLeft: '0.75rem',
  paddingRight: '0.75rem',
  '& .MuiButton-startIcon': { marginRight: '0.375rem' },
  '& .MuiButton-startIcon > *': { fontSize: '1rem' },
  '&:hover': {
    borderColor: theme.palette.text.disabled,
    color: theme.palette.text.primary,
    backgroundColor: 'transparent',
  },
  [theme.breakpoints.up('md')]: {
    alignSelf: 'center',
  },
}));

/**
 * 필터바 좌측 (엑셀 버튼 옆) 에 노출되는 일괄 삭제 버튼.
 * 데스크탑에서 체크박스로 선택된 행들이 있을 때만 표시.
 * danger 톤 — error palette 의 outline 으로 강조.
 */
export const BulkDeleteButton = styled(Button)(({ theme }) => ({
  height: 36,
  fontSize: '0.8125rem',
  color: theme.palette.error.main,
  borderColor: theme.palette.errorBorder,
  borderRadius: '6px',
  textTransform: 'none',
  paddingLeft: '0.75rem',
  paddingRight: '0.75rem',
  '& .MuiButton-startIcon': { marginRight: '0.375rem' },
  '& .MuiButton-startIcon > *': { fontSize: '1rem' },
  '&:hover': {
    borderColor: theme.palette.error.main,
    backgroundColor: theme.palette.errorBg,
  },
  [theme.breakpoints.up('md')]: {
    alignSelf: 'center',
  },
}));
