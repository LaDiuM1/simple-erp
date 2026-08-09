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
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/DeleteOutline';
import { CommonManageModal } from '@/shared/ui/CommonSearchModal';
import { PrimaryPageHeaderButton, CancelPageHeaderButton } from '@/shared/ui/layout/PageHeaderButton';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useDemo } from '@/shared/demo/DemoContext';
import {
  useCreateAcquisitionSourceMutation,
  useDeleteAcquisitionSourceMutation,
  useGetAcquisitionSourcesQuery,
} from '@/features/acquisitionSource/api/acquisitionSourceApi';
import {
  ACQUISITION_SOURCE_TYPE_OPTIONS,
  type AcquisitionSourceInfo,
  type AcquisitionSourceType,
} from '@/features/acquisitionSource/types';
import { acquisitionSourceModalColumns } from './acquisitionSourceModalColumns';

interface Props {
  open: boolean;
  onClose: () => void;
}

/**
 * 영업 명부의 sub-master 인 컨택 경로 관리 모달.
 * 행 우측 삭제 버튼 + 우측 상단 추가 버튼. 수정은 의도적으로 미제공 (텍스트 마스터의 단순성 유지).
 */
export default function AcquisitionSourceManageModal({ open, onClose }: Props) {
  const { writeBlocked } = useDemo();
  const submit = useApiSubmit();
  const sourcesQuery = useGetAcquisitionSourcesQuery();
  const {
    data: sourceData,
    currentData: currentSourceData,
    isLoading: isSourcesLoading,
    isFetching: isSourcesFetching,
    isError: isSourcesError,
    error: sourcesError,
    refetch: refetchSources,
  } = sourcesQuery;
  const [createFn, { isLoading: isCreating }] = useCreateAcquisitionSourceMutation();
  const [deleteFn, { isLoading: isDeleting }] = useDeleteAcquisitionSourceMutation();

  const [showAddForm, setShowAddForm] = useState(false);
  const [pendingDelete, setPendingDelete] = useState<AcquisitionSourceInfo | null>(null);

  const adapterApi = useMemo(
    () => ({
      useList: () => ({
        data: sourceData === undefined ? undefined : toPage(sourceData),
        currentData: currentSourceData === undefined ? undefined : toPage(currentSourceData),
        isLoading: isSourcesLoading,
        isFetching: isSourcesFetching,
        isError: isSourcesError,
        error: sourcesError,
        refetch: refetchSources,
      }),
      rowKey: (row: AcquisitionSourceInfo) => row.id,
    }),
    [
      currentSourceData,
      isSourcesError,
      isSourcesFetching,
      isSourcesLoading,
      refetchSources,
      sourceData,
      sourcesError,
    ],
  );

  const handleCreate = async (name: string, type: AcquisitionSourceType, description: string) => {
    await submit(
      createFn({
        name: name.trim(),
        type,
        description: description.trim() === '' ? null : description.trim(),
      }),
      {
        success: '컨택 경로가 등록되었습니다.',
        error: '등록 중 오류가 발생했습니다.',
        onSuccess: () => setShowAddForm(false),
      },
    );
  };

  const handleConfirmDelete = async () => {
    if (!pendingDelete) return;
    await submit(deleteFn(pendingDelete.id), {
      success: '컨택 경로가 삭제되었습니다.',
      error: '삭제 중 오류가 발생했습니다.',
    });
    setPendingDelete(null);
  };

  const headerActions = (
    <PrimaryPageHeaderButton
      startIcon={<AddIcon />}
      onClick={() => setShowAddForm(true)}
      disabled={writeBlocked}
      sx={{ height: 32, visibility: showAddForm ? 'hidden' : 'visible' }}
    >
      추가
    </PrimaryPageHeaderButton>
  );

  return (
    <>
      <CommonManageModal<AcquisitionSourceInfo, object>
        open={open}
        onClose={onClose}
        title="컨택 경로 관리"
        api={adapterApi}
        column={acquisitionSourceModalColumns}
        emptyMessage="등록된 컨택 경로가 없습니다."
        hidePagination
        headerActions={headerActions}
        rowActions={(row) => (
          <Tooltip title="삭제" arrow>
            <IconButton
              size="small"
              aria-label="삭제"
              onClick={() => setPendingDelete(row)}
              disabled={writeBlocked}
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
        writeBlocked={writeBlocked}
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

function toPage(sources: AcquisitionSourceInfo[]) {
  return {
    content: sources,
    page: 0,
    size: sources.length,
    totalElements: sources.length,
    totalPages: 1,
    hasNext: false,
  };
}

/**
 * 컨택 경로 추가 — MUI Dialog 로 띄움. 메인 모달 위에 nested 되어도 MUI 의 modal stacking 이
 * 자동으로 처리해 내부 Select dropdown / focus trap / ESC 닫기가 정상 동작.
 * 닫힐 때 form state 초기화.
 */
function AddSourceDialog({
  open,
  isCreating,
  writeBlocked,
  onSubmit,
  onCancel,
}: {
  open: boolean;
  isCreating: boolean;
  writeBlocked: boolean;
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
          disabled={!canSubmit || isCreating || writeBlocked}
          sx={{ height: 32 }}
        >
          {isCreating ? '등록 중...' : '등록'}
        </PrimaryPageHeaderButton>
      </DialogActions>
    </Dialog>
  );
}
