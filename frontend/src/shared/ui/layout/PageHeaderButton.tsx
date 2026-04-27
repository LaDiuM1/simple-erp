import Button from '@mui/material/Button';
import { styled } from '@mui/material/styles';

const buttonBase = {
  height: 34,
  paddingLeft: '0.875rem',
  paddingRight: '0.875rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  borderRadius: 0,
  textTransform: 'none' as const,
  letterSpacing: '-0.005em',
  boxShadow: 'none',
  '& .MuiButton-startIcon': { marginRight: '0.375rem' },
  '& .MuiButton-startIcon > *': { fontSize: '1rem' },
};

/**
 * 페이지 헤더 영역의 보조(outlined) 버튼.
 * 표준 액션 (등록/저장/취소) 은 PageHeaderActions 의 design prop 으로 처리하고,
 * 이 버튼은 외부 컨텍스트에서 헤더 톤앤매너만 맞추고 싶을 때 사용 (현재는 GenericList 의 엑셀 다운로드).
 */
export const SecondaryPageHeaderButton = styled(Button)(({ theme }) => ({
  height: 34,
  paddingLeft: '0.875rem',
  paddingRight: '0.875rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  borderRadius: '8px',
  boxShadow: 'none',
  textTransform: 'none',
  letterSpacing: '-0.005em',
  backgroundColor: theme.palette.background.paper,
  color: theme.palette.text.secondary,
  border: `1px solid ${theme.palette.divider}`,
  '& .MuiButton-startIcon': { marginRight: '0.375rem' },
  '& .MuiButton-startIcon > *': { fontSize: '1rem' },
  '&:hover': {
    backgroundColor: theme.palette.background.default,
    borderColor: theme.palette.text.disabled,
    color: theme.palette.text.primary,
  },
}));

/**
 * 페이지 헤더 표준 primary (등록 / 저장) 버튼 — PageHeaderActions 의 design='create'/'save' 와 동일 톤.
 * 모달 등 헤더 외부에서도 동일한 톤을 사용할 때 import.
 */
export const PrimaryPageHeaderButton = styled(Button)(({ theme }) => ({
  ...buttonBase,
  backgroundColor: theme.palette.primary.main,
  color: theme.palette.primary.contrastText,
  fontWeight: 600,
  border: `1px solid ${theme.palette.primary.main}`,
  '&:hover': {
    backgroundColor: theme.palette.primary.dark,
    borderColor: theme.palette.primary.dark,
  },
  '&.Mui-disabled': {
    backgroundColor: theme.palette.primary.main,
    borderColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    opacity: 0.5,
  },
}));

/**
 * 페이지 헤더 표준 cancel 버튼 — PageHeaderActions 의 design='cancel' 와 동일 톤.
 */
export const CancelPageHeaderButton = styled(Button)(({ theme }) => ({
  ...buttonBase,
  backgroundColor: 'transparent',
  color: theme.palette.text.secondary,
  border: `1px solid ${theme.palette.divider}`,
  '&:hover': {
    backgroundColor: theme.palette.background.default,
    borderColor: theme.palette.text.disabled,
    color: theme.palette.text.primary,
  },
  '&.Mui-disabled': {
    color: theme.palette.text.disabled,
    borderColor: theme.palette.divider,
  },
}));
