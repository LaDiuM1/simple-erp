import type { ValidatorMap } from '@/shared/hooks/useFieldValidation';
import type { EquipmentFormValues } from '@/features/equipment/types';

const POSITIVE_NUMBER_RE = /^\d+(\.\d+)?$/;
const MONTHS_RE = /^\d+$/;

export const equipmentValidators: ValidatorMap<EquipmentFormValues> = {
  customerId: (v) => (v === '' ? '고객사를 선택해주세요.' : null),
  productId: (v) => (v === '' ? '제품 모델을 선택해주세요.' : null),
  outputValue: (v) =>
    v.trim() !== '' && !POSITIVE_NUMBER_RE.test(v.trim())
      ? '출력 값은 숫자만 입력해주세요.'
      : null,
  outputUnit: (v, all) =>
    all.outputValue.trim() !== '' && v === '' ? '출력 단위를 선택해주세요.' : null,
  oscillatorWarrantyMonths: (v, all) => validateMonths(v, all),
  generalWarrantyMonths: (v, all) => validateMonths(v, all),
};

/** 보증 개월 — 숫자 검증 + 개월을 입력했으면 기산일도 필요 (만료일 파생 불가 방지). */
function validateMonths(v: string, all: EquipmentFormValues): string | null {
  if (v.trim() === '') return null;
  if (!MONTHS_RE.test(v.trim())) return '개월 수는 숫자만 입력해주세요.';
  if (all.warrantyStartDate === '') return '보증 기산일을 함께 입력해주세요.';
  return null;
}
