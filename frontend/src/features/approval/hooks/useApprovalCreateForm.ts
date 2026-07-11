import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_PATH } from '@/shared/config/menuConfig';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useFieldValidation, type FieldValidation } from '@/shared/hooks/useFieldValidation';
import { useToggle } from '@/shared/hooks/useToggle';
import { useFormState } from '@/shared/ui/GenericForm/useFormState';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import type { AttachedFile } from '@/shared/ui/FileAttachField';
import { useCreateApprovalMutation } from '@/features/approval/api/approvalApi';
import {
  EMPTY_APPROVAL_FORM,
  approvalFormToCreateRequest,
  type ApprovalFormValues,
} from '@/features/approval/types';
import { approvalValidators } from '@/features/approval/validation/approvalValidation';

export interface ApprovalCreateFormState {
  values: ApprovalFormValues;
  update: <K extends keyof ApprovalFormValues>(key: K, v: ApprovalFormValues[K]) => void;
  validation: FieldValidation<ApprovalFormValues>;
  updateAttachments: (update: (prev: AttachedFile[]) => AttachedFile[]) => void;
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

/**
 * 기안 작성 form-state hook — 제목 / 본문 / 결재선 / 첨부 상태 + 상신 확인 → 등록 → 상세 이동.
 * 첨부 업로드는 FileAttachField 가 선택 즉시 처리 — 폼은 fileId 목록만 보유.
 */
export function useApprovalCreateForm(): ApprovalCreateFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const submit = useApiSubmit();

  const { values, updateField: update, setAll } = useFormState<ApprovalFormValues>(() => ({
    ...EMPTY_APPROVAL_FORM,
  }));
  const [confirmOpen, confirm] = useToggle();
  const [createApproval, { isLoading: isSaving }] = useCreateApprovalMutation();

  const validation = useFieldValidation(values, approvalValidators);

  // 업로드 완료 콜백의 stale 스냅샷이 업로드 중 입력한 다른 필드를 덮어쓰지 않도록 prev 기반 반영.
  const updateAttachments = (apply: (prev: AttachedFile[]) => AttachedFile[]) => {
    setAll((prev) => ({ ...prev, attachments: apply(prev.attachments) }));
  };

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    if (!validation.validateAll()) {
      snackbar.error('입력값을 확인해주세요.');
      return;
    }
    if (values.line.length === 0) {
      snackbar.error('결재자를 1명 이상 추가해주세요.');
      return;
    }
    confirm.on();
  };

  const handleConfirmedSubmit = async () => {
    confirm.off();
    // 목록 기본 결재함이 PENDING (내 차례) 이라 본인 기안이 안 보임 — 생성된 문서 상세로 직행.
    const documentId = await submit(createApproval(approvalFormToCreateRequest(values)), {
      success: '기안이 상신되었습니다.',
    });
    if (typeof documentId === 'number') {
      navigate(`${MENU_PATH.APPROVALS}/${documentId}`);
    }
  };

  return {
    values,
    update,
    validation,
    updateAttachments,
    isSaving,
    confirmOpen,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: confirm.off,
    handleCancel: () => navigate(MENU_PATH.APPROVALS),
  };
}
