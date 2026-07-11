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
import { useCreatePostMutation } from '@/features/board/api/boardApi';
import {
  BOARD_CATEGORY_OPTIONS,
  EMPTY_BOARD_FORM,
  boardFormToCreateRequest,
  type BoardFormValues,
} from '@/features/board/types';
import { boardValidators } from '@/features/board/validation/boardValidation';
import type { BoardFormState } from './boardFormState';

export function useBoardCreateForm(): BoardFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const { canWrite } = usePermission(MENU_CODE.BOARDS);

  const { values, updateField: update, setAll } = useFormState<BoardFormValues>(() => ({
    ...EMPTY_BOARD_FORM,
  }));
  const [confirmOpen, confirm] = useToggle();
  const [createPost, { isLoading: isSaving }] = useCreatePostMutation();

  // FileAttachField 함수형 onChange 어댑트 — 업로드 완료 시점의 prev 기반으로 첨부만 갱신.
  const updateAttachments = (updateFiles: (prev: AttachedFile[]) => AttachedFile[]) =>
    setAll((prev) => ({ ...prev, attachments: updateFiles(prev.attachments) }));

  const validation = useFieldValidation(values, boardValidators);

  // NOTICE 는 BOARDS write 권한자만 작성 가능 (BE 서비스도 동일 검증) — 옵션 자체를 숨긴다.
  const categoryOptions = BOARD_CATEGORY_OPTIONS.filter((o) => o.value !== 'NOTICE' || canWrite);

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
    await submit(createPost(boardFormToCreateRequest(values)), {
      success: '등록되었습니다.',
      navigateTo: MENU_PATH[MENU_CODE.BOARDS],
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
    handleCancel: () => navigate(MENU_PATH[MENU_CODE.BOARDS]),
  };
}
