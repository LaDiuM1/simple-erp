import type { FieldValidation } from '@/shared/hooks/useFieldValidation';
import type { EmployeeFormValues } from '@/features/employee/types';

/**
 * 직원 등록 / 수정 폼이 공통으로 가지는 최소 상태.
 * 양쪽 모드에서 재사용되는 섹션 (BasicInfo / Affiliation / Address) 의 props 타입.
 */
export interface EmployeeFormStateBase {
  values: EmployeeFormValues;
  update: <K extends keyof EmployeeFormValues>(key: K, v: EmployeeFormValues[K]) => void;
  validation: FieldValidation<EmployeeFormValues>;
  handleAddressSearch: () => void;
}
