import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { styled } from '@mui/material/styles';

export const FormRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  backgroundColor: theme.palette.background.paper,
  padding: '1.75rem 1.25rem',
  [theme.breakpoints.up('md')]: {
    margin: '-2rem',
    padding: '2.5rem 2.25rem',
  },
}));

export const FormGrid = styled('form')(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '2rem',
  maxWidth: 1180,
  marginLeft: 'auto',
  marginRight: 'auto',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: 'minmax(0, 1fr) 24rem',
    gap: '2.5rem',
    alignItems: 'start',
  },
}));

export const FieldsColumn = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '1.75rem',
  minWidth: 0,
});

export const PreviewColumn = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.875rem',
});

/**
 * 미리보기 영역만 sticky — 메모 카드는 스크롤 따라 흐름.
 */
export const PreviewSticky = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.875rem',
  [theme.breakpoints.up('md')]: {
    position: 'sticky',
    top: '1rem',
  },
}));

export const SectionTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  fontWeight: 600,
  color: theme.palette.text.secondary,
  textTransform: 'uppercase',
  letterSpacing: '0.06em',
  marginBottom: '0.625rem',
  paddingBottom: '0.5rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
}));

export const Field = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
});

export const FieldLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  marginBottom: '0.5rem',
  letterSpacing: '-0.005em',
}));

export const FieldHint = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  marginTop: '0.375rem',
}));

/**
 * InputMode 라디오 — 가로 3분할, 동일 width. 설명은 미리보기 영역으로 이전.
 */
export const InputModeChoices = styled(Box)({
  display: 'grid',
  gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
  gap: '0.5rem',
});

export const InputModeChoice = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  padding: '0.625rem 0.5rem',
  border: `1px solid ${theme.palette.divider}`,
  cursor: 'pointer',
  textAlign: 'center',
  transition: 'background-color 0.15s, border-color 0.15s',
  '&[data-selected="true"]': {
    borderColor: theme.palette.primary.main,
    backgroundColor: theme.palette.primarySubtle,
  },
  '&:hover': {
    borderColor: theme.palette.primary.main,
  },
}));

export const InputModeChoiceLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.875rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.005em',
  '[data-selected="true"] &': {
    color: theme.palette.primary.main,
  },
}));

/** 미리보기 영역의 InputMode 설명 박스 */
export const InputModeHintBox = styled(Box)(({ theme }) => ({
  padding: '0.625rem 0.875rem',
  border: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.default,
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  lineHeight: 1.5,
}));
