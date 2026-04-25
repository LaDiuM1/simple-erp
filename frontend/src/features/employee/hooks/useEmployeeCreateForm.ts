import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_PATH } from '@/shared/config/menuConfig';
import { useDaumPostcode } from '@/shared/hooks/useDaumPostcode';
import { useDebouncedValue } from '@/shared/hooks/useDebouncedValue';
import {
  useFieldValidation,
  type FieldValidation,
} from '@/shared/hooks/useFieldValidation';
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
import type { ApiError } from '@/shared/types/api';

export interface EmployeeCreateFormState {
  values: EmployeeFormValues;
  update: <K extends keyof EmployeeFormValues>(key: K, v: EmployeeFormValues[K]) => void;
  validation: FieldValidation<EmployeeFormValues>;
  loginIdStatus: LoginIdStatus;
  isSaving: boolean;
  confirmOpen: boolean;
  handleAddressSearch: () => void;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

export function useEmployeeCreateForm(): EmployeeCreateFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const openPostcode = useDaumPostcode();

  const [values, setValues] = useState<EmployeeFormValues>(() => ({
    ...EMPTY_EMPLOYEE_FORM,
    joinDate: todayIsoDate(),
  }));
  const [confirmOpen, setConfirmOpen] = useState(false);
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
    setConfirmOpen(true);
  };

  const handleConfirmedSubmit = async () => {
    setConfirmOpen(false);
    try {
      await createEmployee(employeeFormToCreateRequest(values)).unwrap();
      snackbar.success('등록되었습니다.');
      navigate(MENU_PATH.EMPLOYEES);
    } catch (err) {
      snackbar.error((err as ApiError)?.message ?? '저장 중 오류가 발생했습니다.');
    }
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
    closeConfirm: () => setConfirmOpen(false),
    handleCancel: () => navigate(MENU_PATH.EMPLOYEES),
  };
}
