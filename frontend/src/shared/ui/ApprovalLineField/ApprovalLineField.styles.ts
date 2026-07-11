import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';

export const FieldRoot = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.5rem',
});

export const FieldLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
}));

export const SelectRow = styled(Box)({
  display: 'flex',
  gap: '0.5rem',
  alignItems: 'flex-start',
  '& > :first-of-type': {
    flex: 1,
  },
});

export const LineList = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.25rem',
});

export const LineRow = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  gap: '0.75rem',
  padding: '0.375rem 0.625rem',
  border: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.paper,
}));

export const StepOrder = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  fontWeight: 600,
  color: theme.palette.primary.main,
  flexShrink: 0,
  minWidth: '2.5rem',
}));

export const ApproverName = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.primary,
  flex: 1,
  minWidth: 0,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
}));

export const RemoveButton = styled(IconButton)(({ theme }) => ({
  padding: '0.125rem',
  color: theme.palette.text.disabled,
  '&:hover': {
    color: theme.palette.error.main,
    backgroundColor: theme.palette.errorBg,
  },
}));

export const EmptyHint = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.disabled,
  padding: '0.375rem 0.125rem',
}));
