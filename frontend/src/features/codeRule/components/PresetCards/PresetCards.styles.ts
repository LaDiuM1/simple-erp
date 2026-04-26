import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { styled } from '@mui/material/styles';

export const CardsGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fill, minmax(11rem, 1fr))',
  gap: '0.75rem',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: 'repeat(auto-fill, minmax(13.5rem, 1fr))',
  },
}));

export const Card = styled('button', {
  shouldForwardProp: (prop) => prop !== 'selected',
})<{ selected: boolean }>(({ theme, selected }) => ({
  appearance: 'none',
  textAlign: 'left',
  cursor: 'pointer',
  position: 'relative',
  padding: '1rem 1.125rem 1.125rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.5rem',
  borderRadius: 8,
  border: `1.5px solid ${selected ? theme.palette.primary.main : theme.palette.divider}`,
  backgroundColor: selected ? theme.palette.primarySubtle : theme.palette.background.paper,
  boxShadow: selected
    ? `0 1px 0 0 ${theme.palette.primary.main}11, 0 4px 14px -8px ${theme.palette.primary.main}33`
    : '0 1px 2px 0 rgba(15, 23, 42, 0.04)',
  transition: 'background-color 0.15s, border-color 0.15s, box-shadow 0.15s, transform 0.06s',
  '&:hover': {
    borderColor: selected ? theme.palette.primary.main : theme.palette.text.disabled,
    backgroundColor: selected
      ? theme.palette.primarySubtle
      : theme.palette.background.default,
    boxShadow: selected
      ? `0 1px 0 0 ${theme.palette.primary.main}11, 0 6px 18px -8px ${theme.palette.primary.main}44`
      : '0 2px 8px -3px rgba(15, 23, 42, 0.08)',
  },
  '&:active': {
    transform: 'translateY(1px)',
  },
  '&:focus-visible': {
    outline: `2px solid ${theme.palette.primary.main}`,
    outlineOffset: 2,
  },
}));

export const CardCheckMark = styled(Box)(({ theme }) => ({
  position: 'absolute',
  top: '0.625rem',
  right: '0.625rem',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: 18,
  height: 18,
  borderRadius: '50%',
  backgroundColor: theme.palette.primary.main,
  color: theme.palette.primary.contrastText,
  '& svg': {
    fontSize: 12,
  },
}));

export const CardLabel = styled(Typography, {
  shouldForwardProp: (prop) => prop !== 'selected',
})<{ selected: boolean }>(({ theme, selected }) => ({
  fontSize: '0.875rem',
  fontWeight: 600,
  color: selected ? theme.palette.primary.main : theme.palette.text.primary,
  letterSpacing: '-0.005em',
  paddingRight: '1.5rem',
}));

export const CardExample = styled(Typography)(({ theme }) => ({
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
  fontSize: '0.75rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
  whiteSpace: 'nowrap',
  overflow: 'hidden',
  textOverflow: 'ellipsis',
  padding: '0.25rem 0.5rem',
  borderRadius: 3,
  backgroundColor: 'rgba(15, 23, 42, 0.04)',
  alignSelf: 'flex-start',
  maxWidth: '100%',
}));

export const CardDescription = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  lineHeight: 1.5,
}));
