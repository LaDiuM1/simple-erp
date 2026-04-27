import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import IconButton from '@mui/material/IconButton';

export const ModalHeader = styled('div')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.75rem',
  padding: '1rem 1.25rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.paper,
  flexShrink: 0,
}));

export const ModalTitle = styled('div')(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  whiteSpace: 'nowrap',
  minWidth: 0,
  flex: 1,
}));

export const ModalCloseButton = styled(IconButton)(({ theme }) => ({
  padding: '0.25rem',
  color: theme.palette.text.secondary,
  '&:hover': { color: theme.palette.text.primary },
}));

/** 행 표 — `[label]` `[value]` 2-column grid. label 옅은 회색 셀, value 흰 셀. */
export const FieldsTable = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '120px minmax(0, 1fr)',
  overflowY: 'auto',
  flex: 1,
  minHeight: 0,
  [theme.breakpoints.up('sm')]: {
    gridTemplateColumns: '140px minmax(0, 1fr)',
  },
}));

export const FieldLabelCell = styled('div')(({ theme }) => ({
  padding: '0.75rem 1.25rem',
  borderRight: `1px solid ${theme.palette.divider}`,
  borderBottom: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.headerBg,
  color: theme.palette.text.secondary,
  fontSize: '0.8125rem',
  fontWeight: 500,
  display: 'flex',
  alignItems: 'flex-start',
  minHeight: 44,
}));

export const FieldValueCell = styled('div')(({ theme }) => ({
  padding: '0.75rem 1.25rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  color: theme.palette.text.primary,
  fontSize: '0.875rem',
  display: 'flex',
  alignItems: 'flex-start',
  minHeight: 44,
  whiteSpace: 'pre-wrap',
  wordBreak: 'break-word',
}));
