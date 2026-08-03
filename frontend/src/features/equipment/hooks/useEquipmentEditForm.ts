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
import { useUpdateEquipmentMutation } from '@/features/equipment/api/equipmentApi';
import {
  equipmentDetailToFormValues,
  equipmentFormToRequest,
  type EquipmentDetail,
  type EquipmentFormValues,
} from '@/features/equipment/types';
import {
  equipmentValidators,
  validateContractLinkedEquipmentChange,
} from '@/features/equipment/validation/equipmentFormValidation';
import type { EquipmentFormStateBase } from './equipmentFormState';

export interface EquipmentEditFormState extends EquipmentFormStateBase {
  detail: EquipmentDetail;
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
export function useEquipmentEditForm(id: number, detail: EquipmentDetail): EquipmentEditFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const submit = useApiSubmit();

  const { values, updateField: update } = useFormState<EquipmentFormValues>(() =>
    equipmentDetailToFormValues(detail),
  );
  const [confirmOpen, confirm] = useToggle();
  const [updateEquipment, { isLoading: isSaving }] = useUpdateEquipmentMutation();

  const validation = useFieldValidation(values, equipmentValidators);

  const productQuery = useGetProductReferenceQuery(Number(values.productId), {
    skip: values.productId === '',
  });
  const supplierName = detail.contractId !== null
    ? (detail.supplierName ?? '')
    : values.productId === '' ? '' : (productQuery.data?.supplierName ?? '');

  const handleSubmit = (e: React.SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (isSaving) return;
    if (!validation.validateAll()) {
      snackbar.error('입력값을 확인해주세요.');
      return;
    }
    const snapshotError = validateContractLinkedEquipmentChange(detail, values);
    if (snapshotError) {
      snackbar.error(snapshotError);
      return;
    }
    confirm.on();
  };

  const handleConfirmedSubmit = async () => {
    confirm.off();
    await submit(
      updateEquipment({ id, body: equipmentFormToRequest(trimStringValues(values)) }),
      {
        success: '저장되었습니다.',
        navigateTo: `${MENU_PATH[MENU_CODE.EQUIPMENTS]}/${id}`,
      },
    );
  };

  return {
    values,
    update,
    validation,
    supplierName,
    contractLinked: detail.contractId !== null,
    detail,
    isSaving,
    confirmOpen,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: confirm.off,
    handleCancel: () => navigate(`${MENU_PATH[MENU_CODE.EQUIPMENTS]}/${id}`),
  };
}
