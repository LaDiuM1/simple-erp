import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import { styled } from '@mui/material/styles';

const MONOSPACE = 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace';

export const CardRoot = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  border: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.paper,
}));

export const CardHeader = styled(Box)(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '0.75rem',
  padding: '0.625rem 0.875rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.default,
}));

export const CardTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  fontWeight: 700,
  color: theme.palette.text.secondary,
  textTransform: 'uppercase',
  letterSpacing: '0.06em',
}));

export const AddTokenButton = styled(Button)(({ theme }) => ({
  height: 28,
  paddingLeft: '0.625rem',
  paddingRight: '0.75rem',
  fontSize: '0.75rem',
  fontWeight: 600,
  borderRadius: 0,
  textTransform: 'none',
  letterSpacing: '-0.005em',
  boxShadow: 'none',
  backgroundColor: 'transparent',
  color: theme.palette.primary.main,
  border: `1px solid ${theme.palette.primary.main}`,
  '& .MuiButton-startIcon': { marginRight: '0.25rem' },
  '&:hover': { backgroundColor: theme.palette.primarySubtle },
}));

export const CardBody = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.875rem',
  padding: '0.875rem 0.875rem 1rem',
  minHeight: '3.5rem',
});

export const GroupBlock = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.375rem',
});

export const GroupLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.6875rem',
  fontWeight: 600,
  color: theme.palette.text.disabled,
  textTransform: 'uppercase',
  letterSpacing: '0.05em',
}));

export const EmptyHint = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.disabled,
}));

export const ChipsRow = styled(Box)({
  display: 'flex',
  flexWrap: 'wrap',
  gap: '0.375rem',
  alignItems: 'flex-start',
});

/**
 * 토큰 chip — kind 별 색상 차등.
 * - literal: 회색 (배경 default + 점선)
 * - builtin: primary subtle
 * - attribute: primary main 강조
 */
export const Chip = styled(Box)(({ theme }) => ({
  display: 'inline-flex',
  flexDirection: 'column',
  alignItems: 'flex-start',
  padding: '0.25rem 0.5rem',
  borderRadius: 4,
  fontFamily: MONOSPACE,
  fontSize: '0.8125rem',
  fontWeight: 600,
  cursor: 'grab',
  userSelect: 'none',
  border: `1px solid ${theme.palette.divider}`,
  transition: 'box-shadow 0.15s, border-color 0.15s, background-color 0.15s, transform 0.1s',
  '&:active': { cursor: 'grabbing' },
  '&:hover': {
    transform: 'translateY(-1px)',
    boxShadow: '0 2px 4px rgba(15, 23, 42, 0.08)',
  },
  '&[data-kind="literal"]': {
    backgroundColor: theme.palette.background.default,
    color: theme.palette.text.primary,
    borderStyle: 'dashed',
    borderColor: theme.palette.text.disabled,
  },
  '&[data-kind="builtin"]': {
    backgroundColor: theme.palette.primarySubtle,
    color: theme.palette.primary.main,
    borderColor: theme.palette.primary.main,
  },
  '&[data-kind="attribute"]': {
    backgroundColor: theme.palette.primary.main,
    color: theme.palette.primary.contrastText,
    borderColor: theme.palette.primary.main,
  },
}));

export const ChipLabel = styled('span')({
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.125rem',
});

export const ChipMappings = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.0625rem',
  marginTop: '0.25rem',
  paddingTop: '0.25rem',
  borderTop: '1px solid currentColor',
  width: '100%',
  fontSize: '0.6875rem',
  fontWeight: 500,
  opacity: 0.85,
});

export const ChipMappingLine = styled('div')({
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.25rem',
  fontFamily: MONOSPACE,
  '& .arrow': { opacity: 0.6 },
  '& .code': { fontWeight: 700 },
});
