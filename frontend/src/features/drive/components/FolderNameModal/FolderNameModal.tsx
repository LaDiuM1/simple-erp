import * as React from 'react';
import { useState } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import TextField from '@mui/material/TextField';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  useCreateDriveFolderMutation,
  useRenameDriveFolderMutation,
} from '@/features/drive/api/driveApi';
import type { DriveFolderItem } from '@/features/drive/types';

interface Props {
  open: boolean;
  onClose: () => void;
  /** 생성 위치 — 현재 탐색 중인 폴더. 루트면 null. */
  parentId: number | null;
  /** 이름 변경 모드 — 대상 폴더. 미지정 시 생성 모드. */
  folder?: DriveFolderItem;
}

/** 폴더 이름 입력 소형 모달 — 생성 / 이름 변경 겸용. */
export default function FolderNameModal({ open, onClose, parentId, folder }: Props) {
  const isRename = folder !== undefined;
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const [createMut, { isLoading: isCreating }] = useCreateDriveFolderMutation();
  const [renameMut, { isLoading: isRenaming }] = useRenameDriveFolderMutation();

  const [name, setName] = useState(() => folder?.name ?? '');

  React.useEffect(() => {
    if (!open) return;
    setName(folder?.name ?? '');
  }, [open, folder]);

  const isSaving = isCreating || isRenaming;

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    const trimmed = name.trim();
    if (trimmed === '') {
      snackbar.error('폴더 이름을 입력해주세요.');
      return;
    }

    const promise = isRename && folder
      ? renameMut({ id: folder.id, body: { name: trimmed } })
      : createMut({ name: trimmed, parentId });
    await submit(promise, {
      success: isRename ? '폴더 이름이 변경되었습니다.' : '폴더가 생성되었습니다.',
      onSuccess: onClose,
    });
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>{isRename ? '폴더 이름 변경' : '새 폴더'}</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <TextField
            autoFocus
            fullWidth
            size="small"
            label="폴더 이름"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            slotProps={{ htmlInput: { maxLength: 100 } }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={isSaving}>취소</Button>
          <Button type="submit" variant="contained" disabled={isSaving}>
            {isSaving ? '저장 중...' : isRename ? '저장' : '생성'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}
