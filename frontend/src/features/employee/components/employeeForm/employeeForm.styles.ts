import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';

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

export const FieldFull = styled(Box)(({ theme }) => ({
  [theme.breakpoints.up('md')]: {
    gridColumn: '1 / -1',
  },
}));

/** md+ 에서 두 번째 컬럼에 배치 (좌측 빈 칸). 모바일에선 자연 스택. */
export const FieldCol2 = styled(Box)(({ theme }) => ({
  [theme.breakpoints.up('md')]: {
    gridColumn: '2',
  },
}));

export const AddressSearchRow = styled(Box)({
  display: 'flex',
  gap: '0.5rem',
  alignItems: 'stretch',
});

/**
 * AddressSearchRow 의 alignItems: 'stretch' 로 옆 input (filled small ~48px) 과 동일 높이 자동 정렬.
 * height 를 명시하면 stretch 가 무시되어 어긋나므로 지정하지 않음.
 */
export const AddressSearchButton = styled(Button)(({ theme }) => ({
  paddingLeft: '0.875rem',
  paddingRight: '0.875rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
  borderColor: theme.palette.divider,
  flexShrink: 0,
  whiteSpace: 'nowrap',
  '&:hover': {
    borderColor: theme.palette.primary.main,
    color: theme.palette.primary.main,
    backgroundColor: theme.palette.primarySubtle,
  },
}));
