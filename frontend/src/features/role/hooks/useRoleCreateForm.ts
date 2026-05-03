import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useToggle } from '@/shared/hooks/useToggle';
import { useFormState } from '@/shared/ui/GenericForm/useFormState';
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
  const submit = useApiSubmit();
  const [createRole, { isLoading: isSaving }] = useCreateRoleMutation();

  const { values, updateField: setField } = useFormState<RoleFormValues>(() =>
    emptyRoleForm(MATRIX_MENUS),
  );
  const [errors, setErrors] = useState<RoleErrors>({});
  const [confirmOpen, confirm] = useToggle();
  const [codeAvailable, setCodeAvailable] = useState<boolean | null>(null);

  const setPermissions = (next: RoleFormValues['permissions']) => setField('permissions', next);

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
    confirm.on();
  };

  const handleConfirmedSubmit = async () => {
    confirm.off();
    await submit(createRole(roleFormToCreateRequest(trimStringValues(values), MATRIX_MENUS)), {
      success: '권한이 등록되었습니다.',
      navigateTo: MENU_PATH[MENU_CODE.ROLES],
    });
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
    closeConfirm: confirm.off,
    handleCancel: () => navigate(MENU_PATH[MENU_CODE.ROLES]),
  };
}
