import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';

/**
 * 댓글 섹션 박스 — 상단 보더는 의도적으로 없음 (PostBody 의 bottom border 가 구분선 역할).
 */
export const SectionRoot = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  borderLeft: `1px solid ${theme.palette.divider}`,
  borderRight: `1px solid ${theme.palette.divider}`,
  borderBottom: `1px solid ${theme.palette.divider}`,
  display: 'flex',
  flexDirection: 'column',
}));

export const SectionHeader = styled(Box)(({ theme }) => ({
  padding: '0.75rem 1rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  display: 'flex',
  alignItems: 'center',
  gap: '0.375rem',
}));

export const SectionTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.875rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
}));

export const CommentCount = styled('span')(({ theme }) => ({
  fontSize: '0.875rem',
  fontWeight: 500,
  color: theme.palette.primary.main,
}));

export const CommentRow = styled(Box)(({ theme }) => ({
  padding: '0.75rem 1rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.25rem',
  '& + &': {
    borderTop: `1px solid ${theme.palette.divider}`,
  },
}));

export const CommentMeta = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
});

export const CommentAuthor = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
}));

export const CommentDate = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.disabled,
}));

export const CommentContent = styled(Typography)(({ theme }) => ({
  fontSize: '0.875rem',
  color: theme.palette.text.primary,
  lineHeight: 1.6,
  whiteSpace: 'pre-wrap',
  wordBreak: 'break-word',
}));

export const CommentDeleteButton = styled(IconButton)(({ theme }) => ({
  marginLeft: 'auto',
  padding: '0.125rem',
  color: theme.palette.text.disabled,
  '&:hover': {
    color: theme.palette.error.main,
    backgroundColor: theme.palette.errorBg,
  },
}));

export const EmptyComments = styled(Typography)(({ theme }) => ({
  padding: '1.25rem 1rem',
  fontSize: '0.875rem',
  color: theme.palette.text.disabled,
}));

export const CommentForm = styled('form')(({ theme }) => ({
  display: 'flex',
  alignItems: 'flex-start',
  gap: '0.5rem',
  padding: '0.75rem 1rem',
  borderTop: `1px solid ${theme.palette.divider}`,
}));

/** 댓글 등록 버튼 — PageHeader 의 create 톤 (primary filled) 을 입력창 (size=small, 40px) 높이에 맞춤. */
export const CommentSubmitButton = styled(Button)(({ theme }) => ({
  height: 40,
  paddingLeft: '0.875rem',
  paddingRight: '0.875rem',
  fontSize: '0.8125rem',
  fontWeight: 600,
  borderRadius: 0,
  textTransform: 'none',
  letterSpacing: '-0.005em',
  boxShadow: 'none',
  flexShrink: 0,
  backgroundColor: theme.palette.primary.main,
  color: theme.palette.primary.contrastText,
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
