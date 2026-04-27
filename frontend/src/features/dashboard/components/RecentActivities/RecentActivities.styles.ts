import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';

export const ActivityIcon = styled(Box)(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: 32,
  height: 32,
  borderRadius: 8,
  flexShrink: 0,
  color: theme.palette.primary.main,
  backgroundColor: theme.palette.primarySubtle,
}));

export const ActivityTypeLabel = styled('span')(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
}));

export const MetaSeparator = styled('span')(({ theme }) => ({
  display: 'inline-block',
  width: 2,
  height: 2,
  borderRadius: '50%',
  margin: '0 0.5rem',
  backgroundColor: theme.palette.text.disabled,
  verticalAlign: 'middle',
}));
