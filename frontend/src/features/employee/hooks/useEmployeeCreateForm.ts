import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_PATH } from '@/shared/config/menuConfig';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useDaumPostcode } from '@/shared/hooks/useDaumPostcode';
import { useDebouncedValue } from '@/shared/hooks/useDebouncedValue';
import { useFieldValidation } from '@/shared/hooks/useFieldValidation';
import { useToggle } from '@/shared/hooks/useToggle';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  useCheckLoginIdAvailabilityQuery,
  useCreateEmployeeMutation,
} from '@/features/employee/api/employeeApi';
import {
  EMPTY_EMPLOYEE_FORM,
  employeeFormToCreateRequest,
  type EmployeeFormValues,
} from '@/features/employee/types';
import {
  employeeCreateValidators,
  LOGIN_ID_MIN,
  todayIsoDate,
  type LoginIdStatus,
} from '@/features/employee/validation/employeeFormValidation';
import { trimStringValues } from '@/shared/utils/trimStringValues';
import type { EmployeeFormStateBase } from './employeeFormState';

export interface EmployeeCreateFormState extends EmployeeFormStateBase {
  loginIdStatus: LoginIdStatus;
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

export function useEmployeeCreateForm(): EmployeeCreateFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const submit = useApiSubmit();
  const openPostcode = useDaumPostcode();

  const [values, setValues] = useState<EmployeeFormValues>(() => ({
    ...EMPTY_EMPLOYEE_FORM,
    joinDate: todayIsoDate(),
  }));
  const [confirmOpen, confirm] = useToggle();
  const [createEmployee, { isLoading: isSaving }] = useCreateEmployeeMutation();

  const update = <K extends keyof EmployeeFormValues>(key: K, v: EmployeeFormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const validation = useFieldValidation(values, employeeCreateValidators);

  const trimmedLoginId = values.loginId.trim();
  const debouncedLoginId = useDebouncedValue(trimmedLoginId, 400);
  const skipAvailabilityCheck =
    debouncedLoginId === '' || debouncedLoginId.length < LOGIN_ID_MIN;
  const { data: availability, isFetching: isCheckingLoginId } =
    useCheckLoginIdAvailabilityQuery(debouncedLoginId, { skip: skipAvailabilityCheck });

  const loginIdStatus: LoginIdStatus = skipAvailabilityCheck
    ? 'idle'
    : trimmedLoginId !== debouncedLoginId || isCheckingLoginId || !availability
      ? 'checking'
      : availability.available
        ? 'available'
        : 'taken';

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
    if (loginIdStatus === 'checking') {
      snackbar.error('로그인 ID 가용성을 확인 중입니다. 잠시 후 다시 시도해주세요.');
      return;
    }
    if (loginIdStatus === 'taken') {
      snackbar.error('이미 사용 중인 로그인 ID 입니다.');
      return;
    }
    confirm.on();
  };

  const handleConfirmedSubmit = async () => {
    confirm.off();
    const trimmed = trimStringValues(values, { skipKeys: ['password', 'passwordConfirm'] });
    await submit(createEmployee(employeeFormToCreateRequest(trimmed)), {
      success: '등록되었습니다.',
      navigateTo: MENU_PATH.EMPLOYEES,
    });
  };

  return {
    values,
    update,
    validation,
    loginIdStatus,
    isSaving,
    confirmOpen,
    handleAddressSearch,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: confirm.off,
    handleCancel: () => navigate(MENU_PATH.EMPLOYEES),
  };
}
