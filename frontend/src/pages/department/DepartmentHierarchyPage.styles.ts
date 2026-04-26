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
  maxWidth: 960,
  marginLeft: 'auto',
  marginRight: 'auto',
});

interface NodeRowStyleProps {
  level: number;
  isDragging: boolean;
  isDropTarget: boolean;
  canWrite: boolean;
}

export const NodeRow = styled(Box, {
  shouldForwardProp: (prop) =>
    !['level', 'isDragging', 'isDropTarget', 'canWrite'].includes(prop as string),
})<NodeRowStyleProps>(({ theme, level, isDragging, isDropTarget, canWrite }) => ({
  paddingLeft: `${level * 1.75 + 0.5}rem`,
  paddingTop: '0.625rem',
  paddingBottom: '0.625rem',
  paddingRight: '0.5rem',
  display: 'flex',
  alignItems: 'center',
  gap: '0.5rem',
  borderRadius: '4px',
  transition: 'background-color 0.12s, opacity 0.12s, outline-color 0.12s',
  cursor: canWrite ? (isDragging ? 'grabbing' : 'grab') : 'default',
  opacity: isDragging ? 0.4 : 1,
  backgroundColor: isDropTarget ? theme.palette.primarySubtle : 'transparent',
  outline: isDropTarget ? `1px dashed ${theme.palette.primary.main}` : '1px solid transparent',
  outlineOffset: 1,
  '&:hover': {
    backgroundColor: !isDragging && !isDropTarget ? theme.palette.action.hover : undefined,
  },
}));
