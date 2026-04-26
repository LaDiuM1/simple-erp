import * as React from 'react';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_PATH } from '@/shared/config/menuConfig';
import { useDaumPostcode } from '@/shared/hooks/useDaumPostcode';
import { useDebouncedValue } from '@/shared/hooks/useDebouncedValue';
import { useFieldValidation } from '@/shared/hooks/useFieldValidation';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import {
  useCheckCustomerBizRegNoAvailabilityQuery,
  useCreateCustomerMutation,
} from '@/features/customer/api/customerApi';
import {
  EMPTY_CUSTOMER_FORM,
  customerFormToCreateRequest,
  type CustomerFormValues,
} from '@/features/customer/types';
import {
  customerValidators,
  type AvailabilityStatus,
} from '@/features/customer/validation/customerFormValidation';
import type { ApiError } from '@/shared/types/api';
import type { CustomerFormStateBase } from './customerFormState';

export interface CustomerCreateFormState extends CustomerFormStateBase {
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

export function useCustomerCreateForm(): CustomerCreateFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const openPostcode = useDaumPostcode();

  const [values, setValues] = useState<CustomerFormValues>(() => ({ ...EMPTY_CUSTOMER_FORM }));
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [createCustomer, { isLoading: isSaving }] = useCreateCustomerMutation();

  const update = <K extends keyof CustomerFormValues>(key: K, v: CustomerFormValues[K]) =>
    setValues((prev) => ({ ...prev, [key]: v }));

  const validation = useFieldValidation(values, customerValidators);

  const trimmedBizRegNo = values.bizRegNo.trim();
  const debouncedBizRegNo = useDebouncedValue(trimmedBizRegNo, 400);
  const skipBizRegNoCheck =
    debouncedBizRegNo === '' || validation.isInvalid('bizRegNo');
  const { data: bizRegNoAvailability, isFetching: isCheckingBizRegNo } =
    useCheckCustomerBizRegNoAvailabilityQuery(debouncedBizRegNo, { skip: skipBizRegNoCheck });

  const bizRegNoStatus: AvailabilityStatus = skipBizRegNoCheck
    ? 'idle'
    : trimmedBizRegNo !== debouncedBizRegNo || isCheckingBizRegNo || !bizRegNoAvailability
      ? 'checking'
      : bizRegNoAvailability.available
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
    if (bizRegNoStatus === 'checking') {
      snackbar.error('사업자등록번호 중복 확인 중입니다. 잠시 후 다시 시도해주세요.');
      return;
    }
    if (bizRegNoStatus === 'taken') {
      snackbar.error('이미 등록된 사업자등록번호입니다.');
      return;
    }
    setConfirmOpen(true);
  };

  const handleConfirmedSubmit = async () => {
    setConfirmOpen(false);
    try {
      await createCustomer(customerFormToCreateRequest(values)).unwrap();
      snackbar.success('등록되었습니다.');
      navigate(MENU_PATH.CUSTOMERS);
    } catch (err) {
      snackbar.error((err as ApiError)?.message ?? '저장 중 오류가 발생했습니다.');
    }
  };

  return {
    values,
    update,
    validation,
    bizRegNoStatus,
    isSaving,
    confirmOpen,
    handleAddressSearch,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: () => setConfirmOpen(false),
    handleCancel: () => navigate(MENU_PATH.CUSTOMERS),
  };
}
