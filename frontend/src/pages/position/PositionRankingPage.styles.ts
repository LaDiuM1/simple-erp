import Box from '@mui/material/Box';
import { styled } from '@mui/material/styles';

export const PageRoot = styled(Box)(({ theme }) => ({
  margin: '-1rem',
  [theme.breakpoints.up('sm')]: { margin: '-2rem' },
}));

export const PageSurface = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.background.paper,
  padding: '1.5rem 1.25rem',
  display: 'flex',
  flexDirection: 'column',
  [theme.breakpoints.up('md')]: { padding: '2rem' },
}));

export const ContentBox = styled(Box)({
  width: '100%',
  maxWidth: 720,
  marginLeft: 'auto',
  marginRight: 'auto',
  display: 'flex',
  flexDirection: 'column',
  gap: '0.375rem',
});

interface RankRowStyleProps {
  isDragging: boolean;
  isDropTarget: boolean;
  canWrite: boolean;
}

export const RankRow = styled(Box, {
  shouldForwardProp: (prop) =>
    !['isDragging', 'isDropTarget', 'canWrite'].includes(prop as string),
})<RankRowStyleProps>(({ theme, isDragging, isDropTarget, canWrite }) => ({
  paddingTop: '0.75rem',
  paddingBottom: '0.75rem',
  paddingLeft: '0.875rem',
  paddingRight: '0.875rem',
  display: 'flex',
  alignItems: 'center',
  gap: '0.75rem',
  borderRadius: '4px',
  border: `1px solid ${theme.palette.divider}`,
  backgroundColor: isDropTarget
    ? theme.palette.primarySubtle
    : theme.palette.background.paper,
  outline: isDropTarget
    ? `1px dashed ${theme.palette.primary.main}`
    : '1px solid transparent',
  outlineOffset: 1,
  opacity: isDragging ? 0.4 : 1,
  cursor: canWrite ? (isDragging ? 'grabbing' : 'grab') : 'default',
  transition: 'background-color 0.12s, opacity 0.12s, outline-color 0.12s',
  '&:hover': {
    backgroundColor:
      canWrite && !isDragging && !isDropTarget ? theme.palette.action.hover : undefined,
  },
}));

export const RankBadge = styled(Box)(({ theme }) => ({
  minWidth: 28,
  height: 28,
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  borderRadius: 4,
  backgroundColor: theme.palette.primarySubtle,
  color: theme.palette.primary.main,
  fontSize: '0.75rem',
  fontWeight: 700,
  paddingLeft: '0.375rem',
  paddingRight: '0.375rem',
}));

export const HelpText = styled(Box)(({ theme }) => ({
  fontSize: '0.8125rem',
  color: theme.palette.text.secondary,
  marginBottom: '0.875rem',
}));
