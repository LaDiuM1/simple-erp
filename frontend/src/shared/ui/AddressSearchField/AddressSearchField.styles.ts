import { styled } from '@mui/material/styles';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import DialogContent from '@mui/material/DialogContent';

export const AddressFieldsRoot = styled(Box)({
  display: 'contents',
});

export const AddressSearchRow = styled(Box)({
  display: 'flex',
  gap: '0.5rem',
  alignItems: 'stretch',
});

export const AddressRoadRow = styled(Box)(({ theme }) => ({
  [theme.breakpoints.up('md')]: {
    gridColumn: '1 / -1',
  },
}));

export const AddressSearchButton = styled(Button)(({ theme }) => ({
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

export const DialogBody = styled(DialogContent)({
  position: 'relative',
  padding: 0,
  height: 'min(68vh, 520px)',
  minHeight: 360,
  overflow: 'hidden',
});

export const PostcodeContainer = styled('div')({
  width: '100%',
  height: '100%',
});

export const DialogStatus = styled(Box)(({ theme }) => ({
  position: 'absolute',
  inset: 0,
  zIndex: 1,
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  gap: theme.spacing(2),
  padding: theme.spacing(3),
  textAlign: 'center',
  backgroundColor: theme.palette.background.paper,
}));

export const DialogTitleRow = styled(Box)({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  gap: '1rem',
});
