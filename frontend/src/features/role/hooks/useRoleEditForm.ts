import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useUpdateRoleMutation } from '@/features/role/api/roleApi';
import { MATRIX_MENUS } from '@/features/role/components/MenuPermissionMatrix';
import {
  roleDetailToFormValues,
  roleFormToUpdateRequest,
  type RoleDetail,
  type RoleFormValues,
} from '@/features/role/types';
import {
  hasErrors,
  validateRoleForm,
  type RoleErrors,
} from '@/features/role/validation/roleValidation';
import { getErrorMessage } from '@/shared/api/error';

export interface RoleEditFormState {
  values: RoleFormValues;
  errors: RoleErrors;
  isSystem: boolean;
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
 * detail 이 이미 로드된 시점에 호출. 로딩/에러 분기는 outer 가 담당.
 */
export function useRoleEditForm(id: number, detail: RoleDetail): RoleEditFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const [updateRole, { isLoading: isSaving }] = useUpdateRoleMutation();

  const [values, setValues] = useState<RoleFormValues>(() =>
    roleDetailToFormValues(detail, MATRIX_MENUS),
  );
  const [errors, setErrors] = useState<RoleErrors>({});
  const [confirmOpen, setConfirmOpen] = useState(false);

  const setField = <K extends keyof RoleFormValues>(key: K, v: RoleFormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const setPermissions = (next: RoleFormValues['permissions']) =>
    setValues((prev) => ({ ...prev, permissions: next }));

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    const next = validateRoleForm(values, 'edit');
    setErrors(next);
    if (hasErrors(next)) {
      snackbar.error('입력값을 확인해주세요.');
      return;
    }
    setConfirmOpen(true);
  };

  const handleConfirmedSubmit = async () => {
    setConfirmOpen(false);
    try {
      await updateRole({ id, body: roleFormToUpdateRequest(values, MATRIX_MENUS) }).unwrap();
      snackbar.success('권한이 수정되었습니다.');
      navigate(MENU_PATH[MENU_CODE.ROLES]);
    } catch (err) {
      snackbar.error(getErrorMessage(err, '저장 중 오류가 발생했습니다.'));
    }
  };

  return {
    values,
    errors,
    isSystem: detail.system,
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
