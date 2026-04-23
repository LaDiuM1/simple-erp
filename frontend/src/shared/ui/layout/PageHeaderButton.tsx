import Button from '@mui/material/Button';
import { styled } from '@mui/material/styles';

/**
 * 페이지 헤더(PageHeader 우측 액션 슬롯) 전용 버튼.
 * AppLayout의 PageHeader sub-toolbar 톤앤매너에 맞춰 높이 34px / 작은 폰트 / 8px radius 로 통일.
 * 본문 영역의 일반 버튼에는 사용하지 않는다.
 */

const pageHeaderButtonBase = {
  height: 34,
  paddingLeft: '0.875rem',
  paddingRight: '0.875rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  borderRadius: '8px',
  boxShadow: 'none',
  textTransform: 'none' as const,
  letterSpacing: '-0.005em',
  '& .MuiButton-startIcon': { marginRight: '0.375rem' },
  '& .MuiButton-startIcon > *': { fontSize: '1rem' },
};

/** 페이지 헤더의 기본(contained) 버튼. 등록/저장 등 primary 액션용. */
export const PrimaryPageHeaderButton = styled(Button)(({ theme }) => ({
  ...pageHeaderButtonBase,
  backgroundColor: theme.palette.primary.main,
  color: theme.palette.primary.contrastText,
  '&:hover': {
    backgroundColor: theme.palette.primary.dark,
    boxShadow: 'none',
  },
}));

/** 페이지 헤더의 보조(outlined) 버튼. 엑셀 다운로드 등 secondary 액션용. */
export const SecondaryPageHeaderButton = styled(Button)(({ theme }) => ({
  ...pageHeaderButtonBase,
  backgroundColor: theme.palette.background.paper,
  color: theme.palette.text.secondary,
  border: `1px solid ${theme.palette.divider}`,
  '&:hover': {
    backgroundColor: theme.palette.background.default,
    borderColor: theme.palette.text.disabled,
    color: theme.palette.text.primary,
  },
}));
