import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useFieldValidation } from '@/shared/hooks/useFieldValidation';
import { usePermission } from '@/shared/hooks/usePermission';
import { useToggle } from '@/shared/hooks/useToggle';
import { useFormState } from '@/shared/ui/GenericForm/useFormState';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import type { AttachedFile } from '@/shared/ui/FileAttachField';
import { useUpdatePostMutation } from '@/features/board/api/boardApi';
import {
  BOARD_CATEGORY_OPTIONS,
  boardFormToUpdateRequest,
  postDetailToFormValues,
  type BoardFormValues,
  type PostDetail,
} from '@/features/board/types';
import { boardValidators } from '@/features/board/validation/boardValidation';
import type { BoardFormState } from './boardFormState';

/**
 * detail 이 이미 로드된 시점에 호출 (invariant). 로딩/에러는 outer (page + QueryGate) 가 분기 처리.
 */
export function useBoardEditForm(id: number, detail: PostDetail): BoardFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const { canWrite } = usePermission(MENU_CODE.BOARDS);

  const { values, updateField: update, setAll } = useFormState<BoardFormValues>(() =>
    postDetailToFormValues(detail),
  );
  const [confirmOpen, confirm] = useToggle();
  const [updatePost, { isLoading: isSaving }] = useUpdatePostMutation();

  // FileAttachField 함수형 onChange 어댑트 — 업로드 완료 시점의 prev 기반으로 첨부만 갱신.
  const updateAttachments = (updateFiles: (prev: AttachedFile[]) => AttachedFile[]) =>
    setAll((prev) => ({ ...prev, attachments: updateFiles(prev.attachments) }));

  const validation = useFieldValidation(values, boardValidators);

  // NOTICE 는 BOARDS write 권한자만 선택 가능. 기존 글이 NOTICE 면 선택값이 옵션 밖으로
  // 벗어나지 않도록 옵션에 유지 (BE 가 NOTICE 수정 시 write 권한을 재검증).
  const categoryOptions = BOARD_CATEGORY_OPTIONS.filter(
    (o) => o.value !== 'NOTICE' || canWrite || detail.category === 'NOTICE',
  );

  const detailPath = `${MENU_PATH[MENU_CODE.BOARDS]}/${id}`;

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    if (!validation.validateAll()) {
      snackbar.error('입력값을 확인해주세요.');
      return;
    }
    confirm.on();
  };

  const handleConfirmedSubmit = async () => {
    confirm.off();
    await submit(updatePost({ id, body: boardFormToUpdateRequest(values) }), {
      success: '저장되었습니다.',
      navigateTo: detailPath,
    });
  };

  return {
    values,
    update,
    updateAttachments,
    validation,
    categoryOptions,
    isSaving,
    confirmOpen,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: confirm.off,
    handleCancel: () => navigate(detailPath),
  };
}
