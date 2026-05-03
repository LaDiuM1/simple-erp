import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useToggle } from '@/shared/hooks/useToggle';
import { useFormState } from '@/shared/ui/GenericForm/useFormState';
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
import { trimStringValues } from '@/shared/utils/trimStringValues';

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
  const submit = useApiSubmit();
  const [updateRole, { isLoading: isSaving }] = useUpdateRoleMutation();

  const { values, updateField: setField } = useFormState<RoleFormValues>(() =>
    roleDetailToFormValues(detail, MATRIX_MENUS),
  );
  const [errors, setErrors] = useState<RoleErrors>({});
  const [confirmOpen, confirm] = useToggle();

  const setPermissions = (next: RoleFormValues['permissions']) => setField('permissions', next);

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    const next = validateRoleForm(values, 'edit');
    setErrors(next);
    if (hasErrors(next)) {
      snackbar.error('입력값을 확인해주세요.');
      return;
    }
    confirm.on();
  };

  const handleConfirmedSubmit = async () => {
    confirm.off();
    await submit(updateRole({ id, body: roleFormToUpdateRequest(trimStringValues(values), MATRIX_MENUS) }), {
      success: '권한이 수정되었습니다.',
      navigateTo: MENU_PATH[MENU_CODE.ROLES],
    });
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
    closeConfirm: confirm.off,
    handleCancel: () => navigate(MENU_PATH[MENU_CODE.ROLES]),
  };
}
