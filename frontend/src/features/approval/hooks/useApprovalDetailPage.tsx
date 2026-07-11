import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import BlockRoundedIcon from '@mui/icons-material/BlockRounded';
import CheckRoundedIcon from '@mui/icons-material/CheckRounded';
import ReceiptLongRoundedIcon from '@mui/icons-material/ReceiptLongRounded';
import UndoRoundedIcon from '@mui/icons-material/UndoRounded';
import Box from '@mui/material/Box';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useToggle } from '@/shared/hooks/useToggle';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import type { HeaderDetailField } from '@/shared/ui/GenericHeaderDetails';
import { formatDateTime } from '@/shared/ui/GenericTabbedTable';
import {
  useApproveApprovalMutation,
  useCancelApprovalMutation,
  useGetApprovalQuery,
  useRejectApprovalMutation,
} from '@/features/approval/api/approvalApi';
import { APPROVAL_DOC_TYPE_LABELS, type ApprovalDetail } from '@/features/approval/types';
import ApprovalStatusIndicator from '@/features/approval/components/ApprovalStatusIndicator';
import type { ApprovalDetailModalProps } from '@/features/approval/components/ApprovalDetailModals/ApprovalDetailModals';
import type { DecisionMode } from '@/features/approval/components/DecisionModal/DecisionModal';
import { useStepTab } from './useStepTab';
import { useAttachmentTab } from './useAttachmentTab';

/**
 * 전자결재 상세 page hook — fetching / 권한 / 탭 오케스트레이션 / headerActions / 결정 모달 묶음.
 * 내 차례 (myTurn) / 상신 취소 가능 (cancelable) 판단은 BE 가 현재 사용자 관점으로 내려주는 플래그 사용.
 * Hook 은 JSX 반환하지 않는다 (CLAUDE.md). 상세 헤더 필드는 detail 보장된 렌더 시점에
 * `approvalInfoFields(detail)` 를 page 가 호출하도록 builder 만 export.
 */
export function useApprovalDetailPage(approvalId: number) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.APPROVALS);
  const submit = useApiSubmit();

  const detailQuery = useGetApprovalQuery(approvalId, { skip: !approvalId });
  const detail = detailQuery.data;

  const stepTab = useStepTab(
    detail?.steps ?? [],
    detail?.status === 'IN_PROGRESS' ? detail.currentStepOrder : 0,
  );
  const attachmentTab = useAttachmentTab(approvalId, detail?.attachments ?? []);

  const [decision, setDecision] = useState<DecisionMode | null>(null);
  const [cancelOpen, cancelModal] = useToggle();

  const [approveMut, { isLoading: isApproving }] = useApproveApprovalMutation();
  const [rejectMut, { isLoading: isRejecting }] = useRejectApprovalMutation();
  const [cancelMut, { isLoading: isCanceling }] = useCancelApprovalMutation();

  const handleSubmitDecision = async (comment: string) => {
    if (!decision) return;
    const mutation = decision === 'approve' ? approveMut : rejectMut;
    await submit(
      mutation({ id: approvalId, body: { comment: comment.trim() === '' ? null : comment.trim() } }),
      {
        success: decision === 'approve' ? '승인 처리되었습니다.' : '반려 처리되었습니다.',
        error: '결재 처리 중 오류가 발생했습니다.',
        onSuccess: () => setDecision(null),
      },
    );
  };

  const handleConfirmCancel = async () => {
    await submit(cancelMut(approvalId), {
      success: '상신이 취소되었습니다.',
      error: '상신 취소 중 오류가 발생했습니다.',
      onSuccess: cancelModal.off,
    });
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
      onClick: () => navigate(MENU_PATH[MENU_CODE.APPROVALS]),
    },
    // EXPENSE 문서는 원본 경비 청구 (refId) 상세로 교차 진입 — EXPENSES read 권한자만 노출.
    ...(detail?.docType === 'EXPENSE' && detail.refId
      ? [
          {
            design: 'secondary' as const,
            label: '경비 상세',
            icon: <ReceiptLongRoundedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.EXPENSES]}/${detail.refId}`),
            menuCode: MENU_CODE.EXPENSES,
          },
        ]
      : []),
    ...(canWrite && detail?.myTurn
      ? [
          {
            design: 'secondary' as const,
            label: '반려',
            icon: <BlockRoundedIcon sx={{ fontSize: 18 }} />,
            onClick: () => setDecision('reject'),
          },
          {
            design: 'create' as const,
            label: '승인',
            icon: <CheckRoundedIcon sx={{ fontSize: 18 }} />,
            onClick: () => setDecision('approve'),
            menuCode: MENU_CODE.APPROVALS,
          },
        ]
      : []),
    ...(canWrite && detail?.cancelable
      ? [
          {
            design: 'secondary' as const,
            label: '상신 취소',
            icon: <UndoRoundedIcon sx={{ fontSize: 18 }} />,
            onClick: cancelModal.on,
          },
        ]
      : []),
  ];

  const modal: ApprovalDetailModalProps = {
    decision,
    isDeciding: isApproving || isRejecting,
    onCloseDecision: () => setDecision(null),
    onSubmitDecision: handleSubmitDecision,
    cancelOpen,
    isCanceling,
    onConfirmCancel: handleConfirmCancel,
    onCloseCancel: cancelModal.off,
  };

  return {
    queries: { detail: detailQuery },
    headerActions,
    tabsList: [stepTab.tab, attachmentTab.tab],
    modal,
  };
}

export function approvalInfoFields(d: ApprovalDetail): HeaderDetailField[] {
  return [
    { label: '제목', value: d.title, fullWidth: true },
    { label: '유형', value: APPROVAL_DOC_TYPE_LABELS[d.docType] },
    { label: '기안자', value: d.drafterName },
    { label: '상태', value: <ApprovalStatusIndicator status={d.status} /> },
    { label: '기안일', value: formatDateTime(d.createdAt) },
    {
      label: '본문',
      value: d.content ? (
        <Box component="span" sx={{ whiteSpace: 'pre-wrap' }}>
          {d.content}
        </Box>
      ) : (
        d.content
      ),
      fullWidth: true,
    },
  ];
}
