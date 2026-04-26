import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useCreateRoleMutation } from '@/features/role/api/roleApi';
import { MATRIX_MENUS } from '@/features/role/components/MenuPermissionMatrix';
import {
  emptyRoleForm,
  roleFormToCreateRequest,
  type RoleFormValues,
} from '@/features/role/types';
import {
  hasErrors,
  validateRoleForm,
  type RoleErrors,
} from '@/features/role/validation/roleValidation';
import { getErrorMessage } from '@/shared/api/error';

export interface RoleCreateFormState {
  values: RoleFormValues;
  errors: RoleErrors;
  /** 코드가 사용 가능한지 (debounce 후 갱신) */
  codeAvailable: boolean | null;
  setField: <K extends keyof RoleFormValues>(key: K, v: RoleFormValues[K]) => void;
  setPermissions: (next: RoleFormValues['permissions']) => void;
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

interface Options {
  /** code 사용 가능 여부 (CodeAvailability 컴포넌트에서 위임). 없으면 null. */
  codeAvailable: boolean | null;
}

/**
 * 권한 등록 폼 상태 + 제출 흐름.
 * 코드 가용성 검사는 컴포넌트가 외부 hook 으로 처리하고 결과만 전달.
 */
export function useRoleCreateForm({ codeAvailable }: Options): RoleCreateFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const [createRole, { isLoading: isSaving }] = useCreateRoleMutation();

  const [values, setValues] = useState<RoleFormValues>(() => emptyRoleForm(MATRIX_MENUS));
  const [errors, setErrors] = useState<RoleErrors>({});
  const [confirmOpen, setConfirmOpen] = useState(false);

  const setField = <K extends keyof RoleFormValues>(key: K, v: RoleFormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const setPermissions = (next: RoleFormValues['permissions']) =>
    setValues((prev) => ({ ...prev, permissions: next }));

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    const next = validateRoleForm(values, 'create');
    setErrors(next);
    if (hasErrors(next)) {
      snackbar.error('입력값을 확인해주세요.');
      return;
    }
    if (codeAvailable === false) {
      snackbar.error('이미 사용 중인 권한 코드입니다.');
      return;
    }
    setConfirmOpen(true);
  };

  const handleConfirmedSubmit = async () => {
    setConfirmOpen(false);
    try {
      await createRole(roleFormToCreateRequest(values, MATRIX_MENUS)).unwrap();
      snackbar.success('권한이 등록되었습니다.');
      navigate(MENU_PATH[MENU_CODE.ROLES]);
    } catch (err) {
      snackbar.error(getErrorMessage(err, '저장 중 오류가 발생했습니다.'));
    }
  };

  return {
    values,
    errors,
    codeAvailable,
    setField,
    setPermissions,
    isSaving,
    confirmOpen,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: () => setConfirmOpen(false),
    handleCancel: () => navigate(MENU_PATH[MENU_CODE.ROLES]),
  };
}
