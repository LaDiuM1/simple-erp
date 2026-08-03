import type { ValidatorMap } from '@/shared/hooks/useFieldValidation';
import {
  CONTRACT_STATUS,
  contractDetailToFormValues,
  type ContractDetail,
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

const SCHEDULE_ERROR = '진행 상태와 실제 일정 날짜의 흐름을 확인해주세요.';

/** ContractService 저장 경계의 상태별 실제 이정표 및 날짜 순서 불변식을 동일하게 검증한다. */
export function validateContractSchedule(
  values: ContractFormValues,
  businessToday = currentBusinessDate(),
): string | null {
  const hasOrder = values.orderDate !== '';
  const hasArrival = values.arrivalDate !== '';
  const hasInstalled = values.installedDate !== '';
  const hasSettled = values.settledDate !== '';

  const milestonesMatchStatus = (() => {
    switch (values.status) {
      case CONTRACT_STATUS.CONTRACTED:
        return !hasOrder && !hasArrival && !hasInstalled && !hasSettled;
      case CONTRACT_STATUS.ORDERED:
        return hasOrder && !hasArrival && !hasInstalled && !hasSettled;
      case CONTRACT_STATUS.ARRIVED:
      case CONTRACT_STATUS.INSTALLING:
        return hasOrder && hasArrival && !hasInstalled && !hasSettled;
      case CONTRACT_STATUS.INSTALLED:
        return hasOrder && hasArrival && hasInstalled && !hasSettled;
      case CONTRACT_STATUS.SETTLED:
        return hasOrder && hasArrival && hasInstalled && hasSettled;
      case CONTRACT_STATUS.CANCELED:
        return true;
      default:
        return false;
    }
  })();
  if (!milestonesMatchStatus) return SCHEDULE_ERROR;

  const actualDates = [
    values.orderDate,
    values.arrivalDate,
    values.installedDate,
    values.settledDate,
  ].filter((date) => date !== '');
  if (actualDates.some((date) => date > businessToday)) return SCHEDULE_ERROR;

  if (values.contractDate === '') return SCHEDULE_ERROR;
  const orderedFlow = (
    (!hasOrder || values.orderDate >= values.contractDate)
    && (!hasArrival || (hasOrder && values.arrivalDate >= values.orderDate))
    && (!hasInstalled || (hasArrival && values.installedDate >= values.arrivalDate))
    && (!hasSettled || (hasInstalled && values.settledDate >= values.installedDate))
  );
  return orderedFlow ? null : SCHEDULE_ERROR;
}

const INSTALLED_SNAPSHOT_ERROR =
  '설비가 생성된 계약의 고객사·제품·출력·설치일과 완료 상태는 변경할 수 없습니다.';

/** 설치 이벤트가 만든 설비 스냅샷과 계약 완료 상태를 수정 폼에서도 선제적으로 보호한다. */
export function validateInstalledContractChange(
  detail: ContractDetail,
  values: ContractFormValues,
): string | null {
  if (detail.status !== CONTRACT_STATUS.INSTALLED && detail.status !== CONTRACT_STATUS.SETTLED) {
    return null;
  }
  const original = contractDetailToFormValues(detail);
  const statusAllowed = detail.status === CONTRACT_STATUS.SETTLED
    ? values.status === CONTRACT_STATUS.SETTLED
    : values.status === CONTRACT_STATUS.INSTALLED || values.status === CONTRACT_STATUS.SETTLED;
  const snapshotUnchanged = values.customerId === original.customerId
    && values.productId === original.productId
    && sameOptionalNumber(values.outputValue, original.outputValue)
    && values.outputUnit === original.outputUnit
    && values.installedDate === original.installedDate;
  return statusAllowed && snapshotUnchanged ? null : INSTALLED_SNAPSHOT_ERROR;
}

function sameOptionalNumber(left: string, right: string): boolean {
  if (left.trim() === '' || right.trim() === '') return left.trim() === right.trim();
  return Number(left) === Number(right);
}

function currentBusinessDate(): string {
  const parts = new Intl.DateTimeFormat('en-US', {
    timeZone: 'Asia/Seoul',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).formatToParts(new Date());
  const value = Object.fromEntries(parts.map(({ type, value: part }) => [type, part]));
  return `${value.year}-${value.month}-${value.day}`;
}
