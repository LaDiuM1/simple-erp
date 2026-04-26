import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_PATH } from '@/shared/config/menuConfig';
import { useDaumPostcode } from '@/shared/hooks/useDaumPostcode';
import { useFieldValidation } from '@/shared/hooks/useFieldValidation';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { useUpdateEmployeeMutation } from '@/features/employee/api/employeeApi';
import {
  employeeDetailToFormValues,
  employeeFormToUpdateRequest,
  type EmployeeDetail,
  type EmployeeFormValues,
} from '@/features/employee/types';
import { employeeEditValidators } from '@/features/employee/validation/employeeFormValidation';
import { getErrorMessage } from '@/shared/api/error';
import type { EmployeeFormStateBase } from './employeeFormState';

export interface EmployeeEditFormState extends EmployeeFormStateBase {
  detail: EmployeeDetail;
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

/**
 * detail 이 이미 로드된 시점에 호출. 로딩/에러는 호출자 (EmployeeEditForm) 가 분기 처리.
 * detail 을 useState 초기화에만 쓰므로 동일 id 의 detail 이 외부에서 갱신돼도 폼에 반영되지 않음 — 의도된 동작.
 */
export function useEmployeeEditForm(
  id: number,
  detail: EmployeeDetail,
): EmployeeEditFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const openPostcode = useDaumPostcode();

  const [values, setValues] = useState<EmployeeFormValues>(() =>
    employeeDetailToFormValues(detail),
  );
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [updateEmployee, { isLoading: isSaving }] = useUpdateEmployeeMutation();

  const update = <K extends keyof EmployeeFormValues>(key: K, v: EmployeeFormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const validation = useFieldValidation(values, employeeEditValidators);

  const handleAddressSearch = () => {
    openPostcode((data) => {
      update('zipCode', data.zonecode);
      update('roadAddress', data.roadAddress);
    }).catch((err: Error) => {
      snackbar.error(err.message);
    });
  };

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    if (!validation.validateAll()) {
      snackbar.error('입력값을 확인해주세요.');
      return;
    }
    setConfirmOpen(true);
  };

  const handleConfirmedSubmit = async () => {
    setConfirmOpen(false);
    try {
      await updateEmployee({ id, body: employeeFormToUpdateRequest(values) }).unwrap();
      snackbar.success('저장되었습니다.');
      navigate(MENU_PATH.EMPLOYEES);
    } catch (err) {
      snackbar.error(getErrorMessage(err, '저장 중 오류가 발생했습니다.'));
    }
  };

  return {
    values,
    update,
    validation,
    handleAddressSearch,
    detail,
    isSaving,
    confirmOpen,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: () => setConfirmOpen(false),
    handleCancel: () => navigate(MENU_PATH.EMPLOYEES),
  };
}
