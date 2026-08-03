import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useFieldValidation } from '@/shared/hooks/useFieldValidation';
import { useToggle } from '@/shared/hooks/useToggle';
import { useFormState } from '@/shared/ui/GenericForm/useFormState';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { trimStringValues } from '@/shared/utils/trimStringValues';
import { useGetProductReferenceQuery } from '@/features/product/api/productApi';
import { useUpdateContractMutation } from '@/features/contract/api/contractApi';
import {
  contractDetailToFormValues,
  contractFormToUpdateRequest,
  type ContractDetail,
  type ContractFormValues,
} from '@/features/contract/types';
import {
  contractValidators,
  suggestStatus,
  validateContractSchedule,
  validateInstalledContractChange,
} from '@/features/contract/validation/contractFormValidation';
import type { ContractFormStateBase } from './contractFormState';

export interface ContractEditFormState extends ContractFormStateBase {
  detail: ContractDetail;
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

/**
 * detail 이 이미 로드된 시점에 호출. 로딩/에러는 호출자 (page 의 QueryGate) 가 분기 처리.
 */
export function useContractEditForm(id: number, detail: ContractDetail): ContractEditFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const submit = useApiSubmit();

  const { values, updateField: update } = useFormState<ContractFormValues>(() =>
    contractDetailToFormValues(detail),
  );
  const [confirmOpen, confirm] = useToggle();
  const [updateContract, { isLoading: isSaving }] = useUpdateContractMutation();

  const validation = useFieldValidation(values, contractValidators);

  const productQuery = useGetProductReferenceQuery(Number(values.productId), {
    skip: values.productId === '',
  });
  const supplierName =
    values.productId === '' ? '' : (productQuery.data?.supplierName ?? '');

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    if (!validation.validateAll()) {
      snackbar.error('입력값을 확인해주세요.');
      return;
    }
    const scheduleError = validateContractSchedule(values);
    if (scheduleError) {
      snackbar.error(scheduleError);
      return;
    }
    const installedChangeError = validateInstalledContractChange(detail, values);
    if (installedChangeError) {
      snackbar.error(installedChangeError);
      return;
    }
    confirm.on();
  };

  const handleConfirmedSubmit = async () => {
    confirm.off();
    await submit(
      updateContract({ id, body: contractFormToUpdateRequest(trimStringValues(values)) }),
      {
        success: '저장되었습니다.',
        navigateTo: `${MENU_PATH[MENU_CODE.CONTRACTS]}/${id}`,
      },
    );
  };

  return {
    values,
    update,
    validation,
    supplierName,
    statusSuggestion: suggestStatus(values),
    installationBoundary:
      detail.status === 'INSTALLED' || detail.status === 'SETTLED' ? detail.status : null,
    detail,
    isSaving,
    confirmOpen,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: confirm.off,
    handleCancel: () => navigate(`${MENU_PATH[MENU_CODE.CONTRACTS]}/${id}`),
  };
}
