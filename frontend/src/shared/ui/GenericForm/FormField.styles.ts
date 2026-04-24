import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import MenuItem from '@mui/material/MenuItem';
import TextField from '@mui/material/TextField';

/** 단일 필드를 감싸는 wrapper. fullWidth 여부에 따라 grid column span 결정. */
export const FieldItem = styled(Box, {
  shouldForwardProp: (prop) => prop !== 'fullWidth',
})<{ fullWidth?: boolean }>(({ fullWidth, theme }) => ({
  gridColumn: 'span 1',
  [theme.breakpoints.up('md')]: {
    gridColumn: fullWidth ? '1 / -1' : 'span 1',
  },
}));

/** 폼 필드용 MUI TextField 공용 스타일. size="small" 기반. */
export const FormTextField = styled(TextField)({});

export const SelectMenuItem = styled(MenuItem)({});
