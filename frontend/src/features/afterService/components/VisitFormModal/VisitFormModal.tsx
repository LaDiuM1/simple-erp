import * as React from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import {
  modalStateResetKey,
  useResettableState,
} from '@/shared/hooks/useResettableState';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  useCreateServiceVisitMutation,
  useDeleteServiceVisitMutation,
  useGetEngineersQuery,
  useUpdateServiceVisitMutation,
} from '@/features/afterService/api/afterServiceApi';
import type { ServiceVisit, ServiceVisitRequest } from '@/features/afterService/types';

interface FormValues {
  visitDate: string;
  engineerId: string;
  problem: string;
  resolution: string;
}

const EMPTY: FormValues = {
  visitDate: '',
  engineerId: '',
  problem: '',
  resolution: '',
};

interface Props {
  open: boolean;
  onClose: () => void;
  afterServiceId: number;
  /** 수정 모드 — 기존 일지. 미지정 시 등록 모드. */
  visit?: ServiceVisit;
}

/** 방문 일지 등록 / 수정 모달 — 수정 모드에서는 일지 삭제도 이 모달에서 수행. */
export default function VisitFormModal({ open, onClose, afterServiceId, visit }: Props) {
  const isEdit = visit !== undefined;
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const [createMut, { isLoading: isCreating }] = useCreateServiceVisitMutation();
  const [updateMut, { isLoading: isUpdating }] = useUpdateServiceVisitMutation();
  const [deleteMut, { isLoading: isDeleting }] = useDeleteServiceVisitMutation();
  const engineersQuery = useGetEngineersQuery();
  const engineers = (engineersQuery.data ?? []).filter(
    (e) => e.active || (visit != null && e.id === visit.engineerId),
  );

  const resetKey = modalStateResetKey(open, visit?.id ?? `new:${afterServiceId}`);
  const [values, setValues] = useResettableState<FormValues>(
    resetKey,
    () => (visit ? toFormValues(visit) : EMPTY),
  );
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useResettableState(
    resetKey,
    () => false,
  );

  const update = <K extends keyof FormValues>(key: K, v: FormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const isSaving = isCreating || isUpdating;

  const handleSubmit = async (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;

    if (values.visitDate === '') {
      snackbar.error('방문일을 선택해주세요.');
      return;
    }
    if (values.engineerId === '') {
      snackbar.error('담당 엔지니어를 선택해주세요.');
      return;
    }

    const body: ServiceVisitRequest = {
      visitDate: values.visitDate,
      engineerId: Number(values.engineerId),
      problem: emptyToNull(values.problem),
      resolution: emptyToNull(values.resolution),
    };

    const promise = isEdit && visit
      ? updateMut({ id: visit.id, afterServiceId, body })
      : createMut({ afterServiceId, body });
    await submit(promise, {
      success: isEdit ? '방문 일지가 수정되었습니다.' : '방문 일지가 등록되었습니다.',
      onSuccess: onClose,
    });
  };

  const handleDelete = async () => {
    if (!visit) return;
    setDeleteConfirmOpen(false);
    await submit(deleteMut({ id: visit.id, afterServiceId }), {
      success: '방문 일지가 삭제되었습니다.',
      onSuccess: onClose,
    });
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{isEdit ? '방문 일지 수정' : '방문 일지 등록'}</DialogTitle>
      <form onSubmit={handleSubmit} noValidate>
        <DialogContent dividers>
          <Stack spacing={2}>
            <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
              <TextField
                size="small"
                type="date"
                label="방문일"
                required
                value={values.visitDate}
                onChange={(e) => update('visitDate', e.target.value)}
                sx={{ flex: 1 }}
                slotProps={{ inputLabel: { shrink: true } }}
              />
              <TextField
                select
                size="small"
                label="담당 엔지니어"
                required
                value={values.engineerId}
                onChange={(e) => update('engineerId', e.target.value)}
                sx={{ flex: 1 }}
              >
                {engineers.map((e) => (
                  <MenuItem key={e.id} value={String(e.id)}>
                    {e.affiliation ? `${e.name} (${e.affiliation})` : e.name}
                  </MenuItem>
                ))}
              </TextField>
            </Stack>
            <TextField
              size="small"
              label="문제"
              multiline
              minRows={2}
              value={values.problem}
              onChange={(e) => update('problem', e.target.value)}
              placeholder="증상 / 발견 사항"
            />
            <TextField
              size="small"
              label="해결"
              multiline
              minRows={2}
              value={values.resolution}
              onChange={(e) => update('resolution', e.target.value)}
              placeholder="조치 내용"
            />
          </Stack>
        </DialogContent>
        <DialogActions>
          {isEdit && (
            <Button
              color="error"
              onClick={() => setDeleteConfirmOpen(true)}
              disabled={isSaving || isDeleting}
              sx={{ marginRight: 'auto' }}
            >
              삭제
            </Button>
          )}
          <Button onClick={onClose} disabled={isSaving || isDeleting}>취소</Button>
          <Button type="submit" variant="contained" disabled={isSaving || isDeleting}>
            {isSaving ? '저장 중...' : isEdit ? '저장' : '등록'}
          </Button>
        </DialogActions>
      </form>

      <ConfirmModal
        isOpen={deleteConfirmOpen}
        title="방문 일지 삭제"
        message={`${values.visitDate || '선택한'} 방문 일지를 삭제하시겠습니까?`}
        confirmLabel={isDeleting ? '삭제 중...' : '삭제'}
        danger
        confirmDisabled={isDeleting}
        onConfirm={handleDelete}
        onCancel={() => setDeleteConfirmOpen(false)}
      />
    </Dialog>
  );
}

function toFormValues(v: ServiceVisit): FormValues {
  return {
    visitDate: v.visitDate,
    engineerId: String(v.engineerId),
    problem: v.problem ?? '',
    resolution: v.resolution ?? '',
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
