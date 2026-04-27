import { useMemo, useState } from 'react';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogTitle from '@mui/material/DialogTitle';
import IconButton from '@mui/material/IconButton';
import MenuItem from '@mui/material/MenuItem';
import Stack from '@mui/material/Stack';
import TextField from '@mui/material/TextField';
import Tooltip from '@mui/material/Tooltip';
import Typography from '@mui/material/Typography';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/DeleteOutline';
import CommonSearchModal from '@/shared/ui/CommonSearchModal';
import type { ColumnConfig } from '@/shared/ui/GenericList';
import { PrimaryPageHeaderButton, CancelPageHeaderButton } from '@/shared/ui/layout/PageHeaderButton';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { getErrorMessage } from '@/shared/api/error';
import {
  useCreateAcquisitionSourceMutation,
  useDeleteAcquisitionSourceMutation,
  useGetAcquisitionSourcesQuery,
} from '@/features/acquisitionSource/api/acquisitionSourceApi';
import {
  ACQUISITION_SOURCE_TYPE_LABELS,
  ACQUISITION_SOURCE_TYPE_OPTIONS,
  type AcquisitionSourceInfo,
  type AcquisitionSourceType,
} from '@/features/acquisitionSource/types';

interface Props {
  open: boolean;
  onClose: () => void;
}

/**
 * 영업 명부의 sub-master 인 컨택 경로 관리 모달.
 * CommonSearchModal 의 manage 모드 활용 — 행 우측 삭제 버튼 + 우측 상단 추가 버튼.
 * 수정은 의도적으로 미제공 (텍스트 마스터의 단순성 유지).
 */
export default function AcquisitionSourceManageModal({ open, onClose }: Props) {
  const snackbar = useSnackbar();
  const { data: sources = [], isFetching, isError, error, refetch } = useGetAcquisitionSourcesQuery();
  const [createFn, { isLoading: isCreating }] = useCreateAcquisitionSourceMutation();
  const [deleteFn, { isLoading: isDeleting }] = useDeleteAcquisitionSourceMutation();

  const [showAddForm, setShowAddForm] = useState(false);
  const [pendingDelete, setPendingDelete] = useState<AcquisitionSourceInfo | null>(null);

  const adapterApi = useMemo(
    () => ({
      useList: () => ({
        data: {
          content: sources,
          page: 0,
          size: sources.length,
          totalElements: sources.length,
          totalPages: 1,
          hasNext: false,
        },
        isFetching,
        isError,
        error,
        refetch,
      }),
      rowKey: (row: AcquisitionSourceInfo) => row.id,
    }),
    [sources, isFetching, isError, error, refetch],
  );

  const handleCreate = async (name: string, type: AcquisitionSourceType, description: string) => {
    try {
      await createFn({
        name: name.trim(),
        type,
        description: description.trim() === '' ? null : description.trim(),
      }).unwrap();
      snackbar.success('컨택 경로가 등록되었습니다.');
      setShowAddForm(false);
    } catch (err) {
      snackbar.error(getErrorMessage(err, '등록 중 오류가 발생했습니다.'));
    }
  };

  const handleConfirmDelete = async () => {
    if (!pendingDelete) return;
    try {
      await deleteFn(pendingDelete.id).unwrap();
      snackbar.success('컨택 경로가 삭제되었습니다.');
    } catch (err) {
      snackbar.error(getErrorMessage(err, '삭제 중 오류가 발생했습니다.'));
    } finally {
      setPendingDelete(null);
    }
  };

  const columns: ColumnConfig<AcquisitionSourceInfo>[] = [
    {
      key: 'name',
      label: '이름',
      mobilePrimary: true,
      render: (s) => (
        <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
          {s.name}
        </Typography>
      ),
    },
    {
      key: 'type',
      label: '분류',
      render: (s) => ACQUISITION_SOURCE_TYPE_LABELS[s.type],
    },
    { key: 'description', label: '설명' },
  ];

  const headerActions = (
    <PrimaryPageHeaderButton
      startIcon={<AddIcon />}
      onClick={() => setShowAddForm(true)}
      sx={{ height: 32, visibility: showAddForm ? 'hidden' : 'visible' }}
    >
      추가
    </PrimaryPageHeaderButton>
  );

  return (
    <>
      <CommonSearchModal<AcquisitionSourceInfo, object>
        open={open}
        onClose={onClose}
        title="컨택 경로 관리"
        mode="manage"
        api={adapterApi}
        column={columns}
        emptyMessage="등록된 컨택 경로가 없습니다."
        hidePagination
        headerActions={headerActions}
        rowActions={(row) => (
          <Tooltip title="삭제" arrow>
            <IconButton
              size="small"
              aria-label="삭제"
              onClick={() => setPendingDelete(row)}
              sx={{ '&:hover': { color: 'error.main' } }}
            >
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        )}
      />

      <AddSourceDialog
        open={showAddForm}
        isCreating={isCreating}
        onSubmit={handleCreate}
        onCancel={() => setShowAddForm(false)}
      />

      <ConfirmModal
        isOpen={pendingDelete !== null}
        title="컨택 경로 삭제"
        message={
          pendingDelete
            ? `"${pendingDelete.name}" 을(를) 삭제하시겠습니까?\n이미 영업 명부와 연결되어 있어도 연결만 해제되며, 명부 자체는 유지됩니다.`
            : ''
        }
        confirmLabel={isDeleting ? '삭제 중...' : '삭제'}
        danger
        onConfirm={handleConfirmDelete}
        onCancel={() => setPendingDelete(null)}
      />
    </>
  );
}

/**
 * 컨택 경로 추가 — MUI Dialog 로 띄움. 메인 모달 위에 nested 되어도 MUI 의 modal stacking 이
 * 자동으로 처리해 내부 Select dropdown / focus trap / ESC 닫기가 정상 동작.
 * 닫힐 때 form state 초기화.
 */
function AddSourceDialog({
  open,
  isCreating,
  onSubmit,
  onCancel,
}: {
  open: boolean;
  isCreating: boolean;
  onSubmit: (name: string, type: AcquisitionSourceType, description: string) => Promise<void> | void;
  onCancel: () => void;
}) {
  const [name, setName] = useState('');
  const [type, setType] = useState<AcquisitionSourceType | ''>('');
  const [description, setDescription] = useState('');

  const canSubmit = name.trim() !== '' && type !== '';

  const handleClose = () => {
    if (isCreating) return;
    setName('');
    setType('');
    setDescription('');
    onCancel();
  };

  const handleSubmit = async () => {
    if (!canSubmit || isCreating) return;
    await onSubmit(name, type as AcquisitionSourceType, description);
    setName('');
    setType('');
    setDescription('');
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="xs" fullWidth>
      <DialogTitle>컨택 경로 추가</DialogTitle>
      <DialogContent dividers>
        <Stack spacing={1.75} sx={{ pt: 1 }}>
          <TextField
            size="small"
            label="이름"
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="SIMTOS26 / 김창훈부사장명함 / 인터넷조사 등"
            slotProps={{ htmlInput: { maxLength: 100 } }}
          />
          <TextField
            select
            size="small"
            label="분류"
            required
            value={type}
            onChange={(e) => setType(e.target.value as AcquisitionSourceType | '')}
          >
            <MenuItem value="">-</MenuItem>
            {ACQUISITION_SOURCE_TYPE_OPTIONS.map((o) => (
              <MenuItem key={o.value} value={o.value}>
                {o.label}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            size="small"
            label="설명"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="개최년도 / 장소 / 자유 메모"
            multiline
            minRows={2}
            slotProps={{ htmlInput: { maxLength: 500 } }}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <CancelPageHeaderButton onClick={handleClose} disabled={isCreating} sx={{ height: 32 }}>
          취소
        </CancelPageHeaderButton>
        <PrimaryPageHeaderButton
          onClick={handleSubmit}
          disabled={!canSubmit || isCreating}
          sx={{ height: 32 }}
        >
          {isCreating ? '등록 중...' : '등록'}
        </PrimaryPageHeaderButton>
      </DialogActions>
    </Dialog>
  );
}
