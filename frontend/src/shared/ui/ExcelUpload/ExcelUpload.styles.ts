import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';

/** 엑셀 다운로드 버튼과 동일 톤 — 36px 높이, divider outline, 보조 텍스트 색. */
export const ExcelActionButton = styled(Button)(({ theme }) => ({
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

export const HiddenFileInput = styled('input')({
  display: 'none',
});

/** 모달 본문 컨테이너 — 가이드 / 양식 링크 / 드롭존 / 결과를 세로로 배치. */
export const ModalBody = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
  padding: '1rem 1.25rem 1.25rem',
  overflowY: 'auto',
  flex: 1,
  minHeight: 0,
});

export const GuideBox = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.375rem',
  padding: '0.875rem 1rem',
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 4,
  backgroundColor: theme.palette.background.default,
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  lineHeight: 1.6,
}));

export const GuideItem = styled('div')(({ theme }) => ({
  '& strong': {
    color: theme.palette.text.primary,
    fontWeight: 600,
    marginRight: '0.375rem',
  },
}));

/** 양식 다운로드 — 텍스트 링크 톤. 모달 본문 안에서 두드러지지 않도록 inline-flex. */
export const TemplateLink = styled('button')(({ theme }) => ({
  alignSelf: 'flex-start',
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.375rem',
  background: 'none',
  border: 'none',
  padding: '0.25rem 0',
  cursor: 'pointer',
  fontSize: '0.8125rem',
  color: theme.palette.primary.main,
  fontWeight: 500,
  '&:hover': {
    color: theme.palette.primary.dark,
    textDecoration: 'underline',
  },
  '&:disabled': {
    color: theme.palette.text.disabled,
    cursor: 'default',
    textDecoration: 'none',
  },
}));

/** 드래그앤드롭 영역 — 점선 보더, hover/active/busy 데이터 속성으로 톤 변경. */
export const DropZone = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  gap: '0.5rem',
  padding: '2rem 1rem',
  border: `1.5px dashed ${theme.palette.divider}`,
  borderRadius: 6,
  backgroundColor: theme.palette.background.paper,
  color: theme.palette.text.secondary,
  cursor: 'pointer',
  transition: 'border-color 120ms ease, background-color 120ms ease, color 120ms ease',
  outline: 'none',
  '&:hover': {
    borderColor: theme.palette.text.disabled,
    color: theme.palette.text.primary,
  },
  '&:focus-visible': {
    borderColor: theme.palette.primary.main,
  },
  '&[data-active]': {
    borderColor: theme.palette.primary.main,
    backgroundColor: theme.palette.primarySubtle,
    color: theme.palette.primary.main,
  },
  '&[data-busy]': {
    cursor: 'progress',
    opacity: 0.7,
  },
  '& .MuiSvgIcon-root': {
    fontSize: '2rem',
  },
}));

export const DropZoneLabel = styled('div')({
  fontSize: '0.875rem',
  fontWeight: 500,
});

export const DropZoneHint = styled('div')(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.disabled,
}));

export const ResultSummary = styled(Box)(({ theme }) => ({
  display: 'flex',
  gap: '1rem',
  padding: '0.75rem 1rem',
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 4,
  fontSize: '0.875rem',
  color: theme.palette.text.secondary,
  '& strong': {
    color: theme.palette.text.primary,
    fontWeight: 600,
  },
  '&[data-tone="failure"] strong': {
    color: theme.palette.error.main,
  },
  '&[data-tone="success"] strong': {
    color: theme.palette.primary.main,
  },
}));

export const ResultHint = styled('span')(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  lineHeight: 1.5,
}));

/** 결과 에러 표 — 모달 본문에서 가로 스크롤 없이 wrap. */
export const ResultErrorTable = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '64px minmax(120px, auto) 1fr',
  rowGap: 0,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 4,
  overflow: 'hidden',
  fontSize: '0.8125rem',
}));

export const ResultErrorHeader = styled(Box)(({ theme }) => ({
  fontWeight: 600,
  padding: '0.5rem 0.75rem',
  backgroundColor: theme.palette.action.hover,
  color: theme.palette.text.secondary,
  borderBottom: `1px solid ${theme.palette.divider}`,
}));

export const ResultErrorCell = styled(Box)(({ theme }) => ({
  padding: '0.5rem 0.75rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  color: theme.palette.text.primary,
  wordBreak: 'break-word',
}));

export const ModalActionsRow = styled(Box)({
  display: 'flex',
  gap: '0.5rem',
  justifyContent: 'flex-end',
  marginTop: '0.25rem',
});

/** 결과 단계 하단 액션 — 닫기 / 다른 파일 업로드. data-primary 속성으로 강조 톤. */
export const UploadModalSecondary = styled('button')(({ theme }) => ({
  height: 32,
  padding: '0 0.875rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  cursor: 'pointer',
  borderRadius: 4,
  border: `1px solid ${theme.palette.divider}`,
  backgroundColor: 'transparent',
  color: theme.palette.text.secondary,
  '&:hover': {
    borderColor: theme.palette.text.disabled,
    color: theme.palette.text.primary,
  },
  '&[data-primary]': {
    backgroundColor: theme.palette.primary.main,
    borderColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    '&:hover': {
      backgroundColor: theme.palette.primary.dark,
      borderColor: theme.palette.primary.dark,
    },
  },
}));
