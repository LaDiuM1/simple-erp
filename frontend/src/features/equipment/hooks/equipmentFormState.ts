import type { FieldValidation } from '@/shared/hooks/useFieldValidation';
import type { EquipmentFormValues } from '@/features/equipment/types';

/**
 * 설비 등록 / 수정 폼이 공통으로 가지는 최소 상태.
 * 양쪽 모드에서 재사용되는 섹션의 props 타입.
 */
export interface EquipmentFormStateBase {
  values: EquipmentFormValues;
  update: <K extends keyof EquipmentFormValues>(key: K, v: EquipmentFormValues[K]) => void;
  validation: FieldValidation<EquipmentFormValues>;
  /** 선택된 제품 모델에서 파생된 공급사 표시명 — 입력이 아닌 읽기 전용 (BE 가 제품에서 저장). */
  supplierName: string;
}
