import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { styled } from '@mui/material/styles';

export const Section = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.5rem',
  '& + &': {
    marginTop: '1.25rem',
  },
});

export const SectionTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  fontWeight: 700,
  color: theme.palette.text.secondary,
  textTransform: 'uppercase',
  letterSpacing: '0.06em',
}));

export const KindBlock = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  marginBottom: '0.375rem',
});

export const KindLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.875rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
}));

export const KindHint = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  marginLeft: '1.875rem',
  marginTop: '-0.25rem',
}));

export const SeqRow = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  gap: '1rem',
  paddingLeft: '0.25rem',
});

export const SeqValueChip = styled(Box)(({ theme }) => ({
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: theme.palette.primary.main,
  padding: '0.25rem 0.625rem',
  borderRadius: 4,
  backgroundColor: theme.palette.primarySubtle,
  minWidth: '4.5rem',
  textAlign: 'center',
  whiteSpace: 'nowrap',
}));
