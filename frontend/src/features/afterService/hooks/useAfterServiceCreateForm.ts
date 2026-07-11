import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { useApiSubmit } from '@/shared/hooks/useApiSubmit';
import { useFieldValidation } from '@/shared/hooks/useFieldValidation';
import { useToggle } from '@/shared/hooks/useToggle';
import { useFormState } from '@/shared/ui/GenericForm/useFormState';
import { useSnackbar } from '@/shared/ui/feedback/snackbar';
import { trimStringValues } from '@/shared/utils/trimStringValues';
import { useGetEquipmentQuery } from '@/features/equipment/api/equipmentApi';
import {
  useCreateAfterServiceMutation,
  useGetEngineersQuery,
} from '@/features/afterService/api/afterServiceApi';
import {
  EMPTY_AFTER_SERVICE_FORM,
  WARRANTY_DECISION,
  afterServiceFormToCreateRequest,
  type AfterServiceFormValues,
} from '@/features/afterService/types';
import {
  afterServiceValidators,
  suggestWarrantyDecision,
} from '@/features/afterService/validation/afterServiceFormValidation';
import type { AfterServiceFormStateBase } from './afterServiceFormState';

export interface AfterServiceCreateFormState extends AfterServiceFormStateBase {
  isSaving: boolean;
  confirmOpen: boolean;
  handleSubmit: (e: React.SubmitEvent<HTMLFormElement>) => void;
  handleConfirmedSubmit: () => Promise<void>;
  closeConfirm: () => void;
  handleCancel: () => void;
}

export function useAfterServiceCreateForm(): AfterServiceCreateFormState {
  const navigate = useNavigate();
  const snackbar = useSnackbar();
  const submit = useApiSubmit();

  const { values, updateField: update } = useFormState<AfterServiceFormValues>(() => ({
    ...EMPTY_AFTER_SERVICE_FORM,
  }));
  const [confirmOpen, confirm] = useToggle();
  const [createAfterService, { isLoading: isSaving }] = useCreateAfterServiceMutation();

  const validation = useFieldValidation(values, afterServiceValidators);

  const engineersQuery = useGetEngineersQuery();
  const engineers = (engineersQuery.data ?? []).filter((e) => e.active);

  // 설비 연결 시 보증 만료일 조회 → 유상 / 무상 제안 (read-only 참조라 controlled input 오염 없음).
  const equipmentQuery = useGetEquipmentQuery(Number(values.equipmentId), {
    skip: values.equipmentId === '',
  });
  const warrantySuggestion = suggestWarrantyDecision(equipmentQuery.data, values.receivedDate);

  const handleCustomerChange = (id: string, name: string) => {
    update('customerId', id);
    update('customerName', name);
    // 다른 고객사의 설비가 연결된 채 남지 않도록 초기화.
    update('equipmentId', '');
    update('equipmentLabel', '');
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
    await submit(createAfterService(afterServiceFormToCreateRequest(trimStringValues(values))), {
      success: '접수되었습니다.',
      navigateTo: MENU_PATH[MENU_CODE.AFTER_SERVICES],
    });
  };

  return {
    values,
    update,
    validation,
    handleCustomerChange,
    handleWarrantyDecisionChange,
    engineers,
    warrantySuggestion,
    isSaving,
    confirmOpen,
    handleSubmit,
    handleConfirmedSubmit,
    closeConfirm: confirm.off,
    handleCancel: () => navigate(MENU_PATH[MENU_CODE.AFTER_SERVICES]),
  };
}
