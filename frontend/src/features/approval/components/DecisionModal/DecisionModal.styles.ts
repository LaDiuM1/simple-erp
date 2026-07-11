import { styled } from '@mui/material/styles';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import TextField from '@mui/material/TextField';

/** ConfirmModal 과 동일 톤의 소형 다이얼로그 외곽 — 라운드 12 / 패딩 1.75rem / 폭 320~360. */
export const DecisionDialog = styled(Dialog)(({ theme }) => ({
  '& .MuiBackdrop-root': {
    backgroundColor: 'rgb(0 0 0 / 0.4)',
  },
  '& .MuiDialog-paper': {
    borderRadius: '12px',
    minWidth: 320,
    maxWidth: 360,
    width: '100%',
    padding: '1.75rem',
    boxShadow: theme.shadows[4],
    display: 'flex',
    flexDirection: 'column',
    gap: '0.625rem',
  },
}));

export const CommentField = styled(TextField)({
  marginTop: '0.25rem',
  '& .MuiInputBase-root': {
    fontSize: '0.875rem',
  },
});

export const ModalCancelButton = styled(Button)(({ theme }) => ({
  paddingLeft: '1rem',
  paddingRight: '1rem',
  paddingTop: '0.5rem',
  paddingBottom: '0.5rem',
  fontSize: '0.875rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
  borderColor: theme.palette.divider,
  borderWidth: 1.5,
  '&:hover': {
    borderColor: theme.palette.text.secondary,
    borderWidth: 1.5,
    color: theme.palette.text.primary,
    backgroundColor: 'transparent',
  },
}));

export const ModalConfirmButton = styled(Button)({
  paddingLeft: '1rem',
  paddingRight: '1rem',
  paddingTop: '0.5rem',
  paddingBottom: '0.5rem',
  fontSize: '0.875rem',
  fontWeight: 500,
});
