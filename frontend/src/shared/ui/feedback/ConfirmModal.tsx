import { useEffect } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import Typography from '@mui/material/Typography';

interface Props {
  isOpen: boolean;
  title: string;
  message?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  danger?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

export default function ConfirmModal({
  isOpen,
  title,
  message,
  confirmLabel = '확인',
  cancelLabel = '취소',
  danger = false,
  onConfirm,
  onCancel,
}: Props) {
  useEffect(() => {
    if (!isOpen) return;
    const handleKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onCancel();
    };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [isOpen, onCancel]);

  return (
    <Dialog
      open={isOpen}
      onClose={onCancel}
      slotProps={{
        backdrop: { sx: { backgroundColor: 'rgb(0 0 0 / 0.4)' } },
      }}
      PaperProps={{
        sx: (theme) => ({
          borderRadius: '12px',
          minWidth: 320,
          maxWidth: 360,
          width: '100%',
          padding: '1.75rem',
          boxShadow: theme.shadows[4],
          display: 'flex',
          flexDirection: 'column',
          gap: '0.625rem',
        }),
      }}
    >
      <DialogTitle sx={{ p: 0, fontSize: '1rem', fontWeight: 600 }}>{title}</DialogTitle>
      {message && (
        <DialogContent sx={{ p: 0 }}>
          <Typography sx={{ fontSize: '0.875rem', color: 'text.secondary', lineHeight: 1.6 }}>
            {message}
          </Typography>
        </DialogContent>
      )}
      <DialogActions sx={{ p: 0, mt: '0.5rem', gap: '0.5rem' }}>
        <Button
          variant="outlined"
          onClick={onCancel}
          sx={{
            px: '1rem',
            py: '0.5rem',
            fontSize: '0.875rem',
            fontWeight: 500,
            color: 'text.secondary',
            borderColor: 'divider',
            borderWidth: 1.5,
            '&:hover': {
              borderColor: 'text.secondary',
              borderWidth: 1.5,
              color: 'text.primary',
              backgroundColor: 'transparent',
            },
          }}
        >
          {cancelLabel}
        </Button>
        <Button
          variant="contained"
          onClick={onConfirm}
          color={danger ? 'error' : 'primary'}
          sx={{
            px: '1rem',
            py: '0.5rem',
            fontSize: '0.875rem',
            fontWeight: 500,
          }}
        >
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
