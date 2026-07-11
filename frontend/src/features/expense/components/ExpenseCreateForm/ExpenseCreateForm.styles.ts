import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';

export const CreateRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  [theme.breakpoints.up('sm')]: {
    margin: '-2rem',
  },
}));

export const CreateForm = styled('form')(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  [theme.breakpoints.up('md')]: {
    padding: '2rem',
  },
}));

export const FieldGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '1rem',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: '1fr 1fr',
  },
}));

/** 경비 항목 카드 목록 + 추가 버튼 + 합계를 세로로 쌓는 컨테이너. */
export const ItemStack = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '1rem',
});

/** 경비 항목 한 건 — 행 단위 시각 구분을 위한 얇은 보더 카드. */
export const ItemCard = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.75rem',
  padding: '1rem',
  border: `1px solid ${theme.palette.divider}`,
}));

export const ItemCardHeader = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
});

export const ItemCardTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: theme.palette.text.secondary,
}));

/** 항목 입력 필드 그리드 — md 이상 [일자 / 분류 / 금액 / 내용] 한 줄. */
export const ItemFieldGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '0.75rem',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: '160px 140px 160px 1fr',
  },
}));

export const AddItemButton = styled(Button)(({ theme }) => ({
  alignSelf: 'flex-start',
  paddingLeft: '0.875rem',
  paddingRight: '0.875rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
  borderColor: theme.palette.divider,
  '&:hover': {
    borderColor: theme.palette.primary.main,
    color: theme.palette.primary.main,
    backgroundColor: theme.palette.primarySubtle,
  },
}));

/** 합계 행 — 항목 목록 우측 하단 정렬. */
export const TotalRow = styled(Box)({
  display: 'flex',
  alignItems: 'baseline',
  justifyContent: 'flex-end',
  gap: '0.5rem',
});

export const TotalLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
}));

export const TotalValue = styled(Typography)(({ theme }) => ({
  fontSize: '1rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
}));
