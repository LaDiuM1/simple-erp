import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';

/* --------------------------------------------------------------------------
 * Form outer
 * ------------------------------------------------------------------------ */

export const CreateRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  [theme.breakpoints.up('sm')]: {
    margin: '-2rem',
  },
}));

export const CreateForm = styled('form')(({ theme }) => ({
  maxWidth: 960,
  margin: '0 auto',
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  gap: '1.25rem',
  [theme.breakpoints.up('md')]: {
    padding: '2rem',
    gap: '1.5rem',
  },
}));

/* --------------------------------------------------------------------------
 * Hero (그라디언트 + 라이브 아바타)
 * ------------------------------------------------------------------------ */

export const HeroCard = styled(Box)(({ theme }) => ({
  position: 'relative',
  display: 'flex',
  alignItems: 'center',
  gap: '1.25rem',
  padding: '1.75rem',
  borderRadius: 14,
  border: `1px solid ${theme.palette.primaryLight}`,
  background: `linear-gradient(135deg, ${theme.palette.primarySubtle} 0%, #F0F9FF 60%, ${theme.palette.background.paper} 100%)`,
  overflow: 'hidden',
  [theme.breakpoints.down('sm')]: {
    padding: '1.25rem',
    gap: '1rem',
  },
}));

export const HeroAvatar = styled(Box)(({ theme }) => ({
  width: 64,
  height: 64,
  borderRadius: '50%',
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontSize: '1.625rem',
  fontWeight: 700,
  color: theme.palette.primary.contrastText,
  backgroundColor: theme.palette.primary.main,
  boxShadow: '0 8px 20px -6px rgb(59 130 246 / 0.45)',
  flexShrink: 0,
  letterSpacing: '-0.02em',
  userSelect: 'none',
  [theme.breakpoints.down('sm')]: {
    width: 56,
    height: 56,
    fontSize: '1.375rem',
  },
}));

export const HeroAvatarPlaceholder = styled(HeroAvatar)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  color: theme.palette.primary.main,
  border: `1px dashed ${theme.palette.primaryLight}`,
  boxShadow: 'none',
}));

export const HeroBody = styled(Box)({
  minWidth: 0,
  flex: 1,
});

export const HeroEyebrow = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  fontWeight: 700,
  color: theme.palette.primary.main,
  letterSpacing: '0.08em',
  textTransform: 'uppercase',
  marginBottom: '0.375rem',
}));

export const HeroTitle = styled(Typography)(({ theme }) => ({
  fontSize: '1.5rem',
  fontWeight: 700,
  color: theme.palette.text.primary,
  letterSpacing: '-0.02em',
  lineHeight: 1.25,
  marginBottom: '0.25rem',
  [theme.breakpoints.down('sm')]: {
    fontSize: '1.25rem',
  },
}));

export const HeroSubtitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.875rem',
  color: theme.palette.text.secondary,
  letterSpacing: '-0.005em',
}));

/* --------------------------------------------------------------------------
 * Section card
 * ------------------------------------------------------------------------ */

export const SectionSurface = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  border: `1px solid ${theme.palette.divider}`,
  borderRadius: 12,
  padding: '1.5rem',
  [theme.breakpoints.down('sm')]: {
    padding: '1.25rem',
  },
}));

export const SectionHeader = styled(Box)({
  display: 'flex',
  alignItems: 'flex-start',
  gap: '0.75rem',
  marginBottom: '1.25rem',
});

export const SectionIcon = styled(Box)(({ theme }) => ({
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: 32,
  height: 32,
  borderRadius: 8,
  color: theme.palette.primary.main,
  backgroundColor: theme.palette.primarySubtle,
  flexShrink: 0,
}));

export const SectionTitleBox = styled(Box)({
  minWidth: 0,
  paddingTop: '0.0625rem',
});

export const SectionTitle = styled(Typography)(({ theme }) => ({
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: theme.palette.text.primary,
  letterSpacing: '-0.005em',
  lineHeight: 1.3,
}));

export const SectionDescription = styled(Typography)(({ theme }) => ({
  fontSize: '0.75rem',
  color: theme.palette.text.secondary,
  marginTop: '0.125rem',
}));

/* --------------------------------------------------------------------------
 * Field grid
 * ------------------------------------------------------------------------ */

export const FieldGrid = styled(Box)(({ theme }) => ({
  display: 'grid',
  gridTemplateColumns: '1fr',
  gap: '1rem',
  [theme.breakpoints.up('md')]: {
    gridTemplateColumns: '1fr 1fr',
  },
}));

export const FieldFull = styled(Box)(({ theme }) => ({
  [theme.breakpoints.up('md')]: {
    gridColumn: '1 / -1',
  },
}));

/** md+ 에서 두 번째 컬럼에 배치 (좌측 빈 칸). 모바일에선 자연 스택. */
export const FieldCol2 = styled(Box)(({ theme }) => ({
  [theme.breakpoints.up('md')]: {
    gridColumn: '2',
  },
}));

/* --------------------------------------------------------------------------
 * Address search (zipCode + 검색 버튼)
 * ------------------------------------------------------------------------ */

export const AddressSearchRow = styled(Box)({
  display: 'flex',
  gap: '0.5rem',
  alignItems: 'stretch',
});

export const AddressSearchButton = styled(Button)(({ theme }) => ({
  height: 40,
  paddingLeft: '0.875rem',
  paddingRight: '0.875rem',
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: theme.palette.text.secondary,
  borderColor: theme.palette.divider,
  flexShrink: 0,
  whiteSpace: 'nowrap',
  '&:hover': {
    borderColor: theme.palette.primary.main,
    color: theme.palette.primary.main,
    backgroundColor: theme.palette.primarySubtle,
  },
}));
