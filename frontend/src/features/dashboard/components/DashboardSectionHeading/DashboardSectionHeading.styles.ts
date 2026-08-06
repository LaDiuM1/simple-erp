import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { styled } from '@mui/material/styles';

export const SectionHeadingRoot = styled(Box)({
  display: 'flex',
  alignItems: 'baseline',
  gap: '0.625rem 0.875rem',
  flexWrap: 'wrap',
});

export const SectionHeadingTitle = styled('h2')(({ theme }) => ({
  margin: 0,
  color: theme.palette.text.primary,
  fontSize: '1rem',
  fontWeight: 700,
  letterSpacing: '-0.015em',
}));

export const SectionDescription = styled(Typography)(({ theme }) => ({
  color: theme.palette.text.secondary,
  fontSize: '0.8125rem',
}));
