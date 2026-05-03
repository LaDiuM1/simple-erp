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
import { trimStringValues } from '@/shared/utils/trimStringValues';

export interface RoleCreateFormState {
  values: RoleFormValues;
  errors: RoleErrors;
  /** 코드가 사용 가능한지 (debounce 후 갱신) */
  codeAvailable: boolean | null;
  setCodeAvailable: (available: boolean | null) => void;
  setField: <K extends keyof RoleFormValues>(key: K, v: RoleFormValues[K]) => void;
  setPermissions: (next: RoleFormValues['permissions']) => void;
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

/**
 * 권한 등록 폼 상태 + 제출 흐름.
 * 코드 가용성 검사 결과는 hook 이 owner — CodeField 컴포넌트가 setCodeAvailable 로 보고.
 */
export function useRoleCreateForm(): RoleCreateFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const [createRole, { isLoading: isSaving }] = useCreateRoleMutation();

  const [values, setValues] = useState<RoleFormValues>(() => emptyRoleForm(MATRIX_MENUS));
  const [errors, setErrors] = useState<RoleErrors>({});
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [codeAvailable, setCodeAvailable] = useState<boolean | null>(null);

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
      await createRole(roleFormToCreateRequest(trimStringValues(values), MATRIX_MENUS)).unwrap();
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
    setCodeAvailable,
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
