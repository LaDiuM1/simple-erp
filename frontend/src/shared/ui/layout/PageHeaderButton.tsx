import Button from '@mui/material/Button';
import { styled } from '@mui/material/styles';

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
