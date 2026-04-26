import type { FieldValidation } from '@/shared/hooks/useFieldValidation';
import type { CustomerFormValues } from '@/features/customer/types';
import type { AvailabilityStatus } from '@/features/customer/validation/customerFormValidation';

/**
 * 고객사 등록 / 수정 폼이 공통으로 가지는 최소 상태.
 * 양쪽 모드에서 재사용되는 섹션의 props 타입.
 */
export interface CustomerFormStateBase {
  values: CustomerFormValues;
  update: <K extends keyof CustomerFormValues>(key: K, v: CustomerFormValues[K]) => void;
  validation: FieldValidation<CustomerFormValues>;
  handleAddressSearch: () => void;
  bizRegNoStatus: AvailabilityStatus;
}
