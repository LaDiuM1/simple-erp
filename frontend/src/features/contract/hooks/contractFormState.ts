import type { FieldValidation } from '@/shared/hooks/useFieldValidation';
import type { ContractFormValues, ContractStatus } from '@/features/contract/types';

/**
 * 계약 등록 / 수정 폼이 공통으로 가지는 최소 상태.
 * 양쪽 모드에서 재사용되는 섹션의 props 타입.
 */
export interface ContractFormStateBase {
  values: ContractFormValues;
  update: <K extends keyof ContractFormValues>(key: K, v: ContractFormValues[K]) => void;
  validation: FieldValidation<ContractFormValues>;
  /** 선택된 제품 모델에서 파생된 공급사 표시명 — 입력이 아닌 읽기 전용 (BE 가 제품에서 저장). */
  supplierName: string;
  /** 마일스톤 일자 기반 상태 제안 — null 이면 현재 상태와 일치 (제안 없음). */
  statusSuggestion: ContractStatus | null;
}
