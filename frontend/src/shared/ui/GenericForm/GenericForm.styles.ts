import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';

/** 폼 페이지의 full-bleed 외곽 — 목록과 동일한 margin 탈출 + 스크롤 영역. */
export const FormRoot = styled(Box)(({ theme }) => ({
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

/** 폼 본문 surface. 내부 스크롤 + 패딩. */
export const FormSurface = styled('form')(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  overflow: 'auto',
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
  minHeight: 0,
  [theme.breakpoints.up('md')]: {
    flex: 1,
    padding: '1.75rem 2rem',
  },
}));

/** 2열 그리드 (md+). 모바일 1열. FormField 가 span 결정. */
export const FormGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '0.75rem',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: '1fr 1fr',
  },
}));

/** 저장 중 인라인 에러 박스 (서버 응답 메시지 노출). */
export const ErrorBox = styled(Box)(({ theme }) => ({
  padding: '0.6875rem 0.875rem',
  backgroundColor: theme.palette.error.light,
  border: `1px solid ${theme.palette.errorBorder}`,
  borderRadius: 4,
  color: theme.palette.error.main,
  fontSize: '0.875rem',
}));

/** 로딩 영역 (수정 모드에서 상세 조회 중). */
export const LoadingCenter = styled(Box)({
  display: 'flex',
  justifyContent: 'center',
  padding: '4rem 0',
});

/** PageHeader 에 주입되는 취소 버튼. */
export const CancelHeaderButton = styled(Button)(({ theme }) => ({
  height: 34,
  paddingLeft: '0.875rem',
  paddingRight: '0.875rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  borderRadius: '8px',
  textTransform: 'none',
  letterSpacing: '-0.005em',
  color: theme.palette.text.secondary,
  borderColor: theme.palette.divider,
  '&:hover': {
    borderColor: theme.palette.text.disabled,
    color: theme.palette.text.primary,
    backgroundColor: 'transparent',
  },
}));
