import type { FieldValidation } from '@/shared/hooks/useFieldValidation';
import type { AfterServiceFormValues, Engineer } from '@/features/afterService/types';
import type { WarrantySuggestion } from '@/features/afterService/validation/afterServiceFormValidation';

type AfterServiceFormUpdate = <K extends keyof AfterServiceFormValues>(
  key: K,
  value: AfterServiceFormValues[K],
) => void;

/** 고객사가 바뀌면 이전 고객사 설비 참조를 같은 상태 변경 경계에서 제거한다. */
export function changeAfterServiceCustomer(
  update: AfterServiceFormUpdate,
  id: string,
  name: string,
): void {
  update('customerId', id);
  update('customerName', name);
  update('equipmentId', '');
  update('equipmentLabel', '');
}

/**
 * AS 접수 / 수정 폼이 공통으로 가지는 최소 상태.
 * 양쪽 모드에서 재사용되는 섹션의 props 타입.
 */
export interface AfterServiceFormStateBase {
  values: AfterServiceFormValues;
  update: AfterServiceFormUpdate;
  validation: FieldValidation<AfterServiceFormValues>;
  /** 고객사 변경 — 다른 고객사 설비가 남지 않도록 설비 선택을 함께 초기화. */
  handleCustomerChange: (id: string, name: string) => void;
  /** 유상 / 무상 판정 변경 — 유상이 아니게 되면 청구액을 함께 비워 모순 입력을 방지. */
  handleWarrantyDecisionChange: (decision: string) => void;
  /** 엔지니어 select 옵션 — 활성 엔지니어만. */
  engineers: Engineer[];
  /** 설비 보증 기반 유상 / 무상 제안 — null 이면 제안 없음. */
  warrantySuggestion: WarrantySuggestion | null;
}
