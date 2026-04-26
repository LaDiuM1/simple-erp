import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { styled } from '@mui/material/styles';

export const FormRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  backgroundColor: theme.palette.background.paper,
  padding: '1.75rem 1.25rem',
  [theme.breakpoints.up('md')]: {
    margin: '-2rem',
    padding: '2.5rem 2.25rem',
  },
}));

export const FormGrid = styled('form')(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '2rem',
  maxWidth: 1180,
  marginLeft: 'auto',
  marginRight: 'auto',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: 'minmax(0, 1fr) 24rem',
    gap: '2.5rem',
    alignItems: 'start',
  },
}));

export const FieldsColumn = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
  gap: '1.75rem',
  minWidth: 0,
});

export const PreviewColumn = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '0.875rem',
  [theme.breakpoints.up('md')]: {
    position: 'sticky',
    top: '1rem',
  },
}));

export const SectionTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  fontWeight: 600,
  color: theme.palette.text.secondary,
  textTransform: 'uppercase',
  letterSpacing: '0.06em',
  marginBottom: '0.625rem',
  paddingBottom: '0.5rem',
  borderBottom: `1px solid ${theme.palette.divider}`,
}));

export const Field = styled(Box)({
  display: 'flex',
  flexDirection: 'column',
});

export const FieldLabel = styled(Typography)(({ theme }) => ({
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  marginBottom: '0.5rem',
  letterSpacing: '-0.005em',
}));

export const FieldHint = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  marginTop: '0.375rem',
}));

export const InlineEditorRow = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '1.25rem',
  alignItems: 'start',
  [theme.breakpoints.up('sm')]: {
    gridTemplateColumns: 'minmax(0, 16rem) minmax(0, 1fr)',
    gap: '1.5rem',
  },
}));

export const TargetReadout = styled(Box)(({ theme }) => ({
  display: 'inline-flex',
  flexDirection: 'column',
  alignSelf: 'flex-start',
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 0,
  backgroundColor: theme.palette.background.paper,
  overflow: 'hidden',
  minWidth: '12rem',
  '& .label': {
    fontSize: '0.875rem',
    fontWeight: 600,
    color: theme.palette.text.secondary,
    backgroundColor: theme.palette.headerBg,
    padding: '0.5rem 1rem',
    textAlign: 'center',
    borderBottom: `1px solid ${theme.palette.divider}`,
  },
  '& .value': {
    fontSize: '1rem',
    fontWeight: 600,
    color: theme.palette.text.primary,
    letterSpacing: '-0.01em',
    lineHeight: 1.2,
    padding: '0.625rem 1rem',
    textAlign: 'center',
  },
}));

export const SliderRow = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  gap: '1.25rem',
  paddingLeft: '0.25rem',
});

export const SliderValueChip = styled(Box)(({ theme }) => ({
  fontFamily: 'ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace',
  fontSize: '0.8125rem',
  fontWeight: 600,
  color: theme.palette.primary.main,
  padding: '0.25rem 0.625rem',
  borderRadius: 4,
  backgroundColor: theme.palette.primarySubtle,
  minWidth: '5rem',
  textAlign: 'center',
  whiteSpace: 'nowrap',
}));

export const AdvancedSection = styled(Box)(({ theme }) => ({
  display: 'flex',
  flexDirection: 'column',
  gap: '1.5rem',
  padding: '1.5rem 1.625rem',
  borderRadius: 8,
  border: `1px solid ${theme.palette.divider}`,
  backgroundColor: theme.palette.background.default,
}));

export const AdvancedTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  fontWeight: 700,
  color: theme.palette.primary.main,
  textTransform: 'uppercase',
  letterSpacing: '0.06em',
  marginBottom: '-0.25rem',
}));
