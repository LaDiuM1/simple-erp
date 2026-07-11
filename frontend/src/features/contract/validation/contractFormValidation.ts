import type { ValidatorMap } from '@/shared/hooks/useFieldValidation';
import {
  CONTRACT_STATUS,
  type ContractFormValues,
  type ContractStatus,
} from '@/features/contract/types';

const POSITIVE_NUMBER_RE = /^\d+(\.\d+)?$/;
const AMOUNT_RE = /^\d+$/;

export const contractValidators: ValidatorMap<ContractFormValues> = {
  customerId: (v) => (v === '' ? '고객사를 선택해주세요.' : null),
  employeeId: (v) => (v === '' ? '계약자를 선택해주세요.' : null),
  productId: (v) => (v === '' ? '제품 모델을 선택해주세요.' : null),
  outputValue: (v) =>
    v.trim() !== '' && !POSITIVE_NUMBER_RE.test(v.trim())
      ? '출력 값은 숫자만 입력해주세요.'
      : null,
  outputUnit: (v, all) =>
    all.outputValue.trim() !== '' && v === '' ? '출력 단위를 선택해주세요.' : null,
  initialAmount: (v) =>
    v.trim() !== '' && !AMOUNT_RE.test(v.trim()) ? '금액은 숫자만 입력해주세요.' : null,
  finalAmount: (v) => {
    if (v.trim() === '') return '최종 계약금액을 입력해주세요.';
    return AMOUNT_RE.test(v.trim()) ? null : '금액은 숫자만 입력해주세요.';
  },
  contractDate: (v) => (v === '' ? '계약일을 선택해주세요.' : null),
};

/**
 * 마일스톤 일자 입력에서 제안되는 상태 — 강제하지 않고 상태 select 의 helperText 로만 노출.
 * 계약취소는 일자와 무관한 사용자 결정이라 제안하지 않는다.
 */
export function suggestStatus(values: ContractFormValues): ContractStatus | null {
  if (values.status === CONTRACT_STATUS.CANCELED) return null;

  let suggested: ContractStatus = CONTRACT_STATUS.CONTRACTED;
  if (values.orderDate !== '') suggested = CONTRACT_STATUS.ORDERED;
  if (values.arrivalDate !== '') suggested = CONTRACT_STATUS.ARRIVED;
  if (values.installedDate !== '') suggested = CONTRACT_STATUS.INSTALLED;
  if (values.settledDate !== '') suggested = CONTRACT_STATUS.SETTLED;

  return suggested === values.status ? null : suggested;
}
