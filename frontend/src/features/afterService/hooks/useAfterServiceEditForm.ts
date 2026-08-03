import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useFieldValidation } from '@/shared/hooks/useFieldValidation';
import { useToggle } from '@/shared/hooks/useToggle';
import { useFormState } from '@/shared/ui/GenericForm/useFormState';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { trimStringValues } from '@/shared/utils/trimStringValues';
import { useGetEquipmentReferenceQuery } from '@/features/equipment/api/equipmentApi';
import {
  useGetEngineersQuery,
  useUpdateAfterServiceMutation,
} from '@/features/afterService/api/afterServiceApi';
import {
  WARRANTY_DECISION,
  afterServiceDetailToFormValues,
  afterServiceFormToUpdateRequest,
  type AfterServiceDetail,
  type AfterServiceFormValues,
} from '@/features/afterService/types';
import {
  afterServiceValidators,
  suggestWarrantyDecision,
} from '@/features/afterService/validation/afterServiceFormValidation';
import { eligibleEngineerOptions } from '@/features/afterService/utils/eligibleEngineerOptions';
import {
  changeAfterServiceCustomer,
  type AfterServiceFormStateBase,
} from './afterServiceFormState';

export interface AfterServiceEditFormState extends AfterServiceFormStateBase {
  detail: AfterServiceDetail;
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
export function useAfterServiceEditForm(
  id: number,
  detail: AfterServiceDetail,
): AfterServiceEditFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const submit = useApiSubmit();

  const { values, updateField: update } = useFormState<AfterServiceFormValues>(() =>
    afterServiceDetailToFormValues(detail),
  );
  const [confirmOpen, confirm] = useToggle();
  const [updateAfterService, { isLoading: isSaving }] = useUpdateAfterServiceMutation();

  const validation = useFieldValidation(values, afterServiceValidators);

  const engineersQuery = useGetEngineersQuery();
  // 활성 + 현재 배정된 엔지니어 (비활성 전환됐어도 기존 배정 유지 표시).
  const engineers = eligibleEngineerOptions(
    engineersQuery.data ?? [],
    values.assignedEngineerId,
  );

  const equipmentQuery = useGetEquipmentReferenceQuery(
    {
      id: Number(values.equipmentId),
      customerId: Number(values.customerId),
    },
    { skip: values.equipmentId === '' || values.customerId === '' },
  );
  const warrantySuggestion = suggestWarrantyDecision(equipmentQuery.data, values.receivedDate);

  const handleCustomerChange = (id2: string, name: string) => {
    changeAfterServiceCustomer(update, id2, name);
  };

  const handleWarrantyDecisionChange = (decision: string) => {
    update('warrantyDecision', decision);
    // 유상이 아니게 되면 청구액을 비워 모순 입력 방지.
    if (decision !== WARRANTY_DECISION.PAID) {
      update('billingAmount', '');
    }
  };

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
    await submit(
      updateAfterService({ id, body: afterServiceFormToUpdateRequest(trimStringValues(values)) }),
      {
        success: '저장되었습니다.',
        navigateTo: `${MENU_PATH[MENU_CODE.AFTER_SERVICES]}/${id}`,
      },
    );
  };

  return {
    values,
    update,
    validation,
    handleCustomerChange,
    handleWarrantyDecisionChange,
    engineers,
    warrantySuggestion,
    detail,
    isSaving,
    confirmOpen,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: confirm.off,
    handleCancel: () => navigate(`${MENU_PATH[MENU_CODE.AFTER_SERVICES]}/${id}`),
  };
}
