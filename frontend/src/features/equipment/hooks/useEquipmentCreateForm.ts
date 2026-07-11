import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useFieldValidation } from '@/shared/hooks/useFieldValidation';
import { useToggle } from '@/shared/hooks/useToggle';
import { useFormState } from '@/shared/ui/GenericForm/useFormState';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { trimStringValues } from '@/shared/utils/trimStringValues';
import { useGetProductQuery } from '@/features/product/api/productApi';
import { useCreateEquipmentMutation } from '@/features/equipment/api/equipmentApi';
import {
  EMPTY_EQUIPMENT_FORM,
  equipmentFormToRequest,
  type EquipmentFormValues,
} from '@/features/equipment/types';
import { equipmentValidators } from '@/features/equipment/validation/equipmentFormValidation';
import type { EquipmentFormStateBase } from './equipmentFormState';

export interface EquipmentCreateFormState extends EquipmentFormStateBase {
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

export function useEquipmentCreateForm(): EquipmentCreateFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const submit = useApiSubmit();

  const { values, updateField: update } = useFormState<EquipmentFormValues>(() => ({
    ...EMPTY_EQUIPMENT_FORM,
  }));
  const [confirmOpen, confirm] = useToggle();
  const [createEquipment, { isLoading: isSaving }] = useCreateEquipmentMutation();

  const validation = useFieldValidation(values, equipmentValidators);

  // 공급사는 입력받지 않고 선택된 제품에서 파생 표시 — 저장도 BE 가 제품 기준으로 수행.
  const productQuery = useGetProductQuery(Number(values.productId), {
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
    await submit(createEquipment(equipmentFormToRequest(trimStringValues(values))), {
      success: '등록되었습니다.',
      navigateTo: MENU_PATH[MENU_CODE.EQUIPMENTS],
    });
  };

  return {
    values,
    update,
    validation,
    supplierName,
    isSaving,
    confirmOpen,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: confirm.off,
    handleCancel: () => navigate(MENU_PATH[MENU_CODE.EQUIPMENTS]),
  };
}
