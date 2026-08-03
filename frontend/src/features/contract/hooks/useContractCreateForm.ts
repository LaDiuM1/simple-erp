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
import { useCreateContractMutation } from '@/features/contract/api/contractApi';
import {
  EMPTY_CONTRACT_FORM,
  contractFormToCreateRequest,
  type ContractFormValues,
} from '@/features/contract/types';
import {
  contractValidators,
  suggestStatus,
} from '@/features/contract/validation/contractFormValidation';
import type { ContractFormStateBase } from './contractFormState';

export interface ContractCreateFormState extends ContractFormStateBase {
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

export function useContractCreateForm(): ContractCreateFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const submit = useApiSubmit();

  const { values, updateField: update } = useFormState<ContractFormValues>(() => ({
    ...EMPTY_CONTRACT_FORM,
  }));
  const [confirmOpen, confirm] = useToggle();
  const [createContract, { isLoading: isSaving }] = useCreateContractMutation();

  const validation = useFieldValidation(values, contractValidators);

  // 공급사는 입력받지 않고 선택된 제품에서 파생 표시 — 저장도 BE 가 제품 기준으로 수행.
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
    confirm.on();
  };

  const handleConfirmedSubmit = async () => {
    confirm.off();
    await submit(createContract(contractFormToCreateRequest(trimStringValues(values))), {
      success: '등록되었습니다.',
      navigateTo: MENU_PATH[MENU_CODE.CONTRACTS],
    });
  };

  return {
    values,
    update,
    validation,
    supplierName,
    statusSuggestion: suggestStatus(values),
    isSaving,
    confirmOpen,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: confirm.off,
    handleCancel: () => navigate(MENU_PATH[MENU_CODE.CONTRACTS]),
  };
}
