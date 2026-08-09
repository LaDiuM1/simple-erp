import type { SalesCustomerAggregate } from '@/features/salesCustomer/types';
import type { ApiError } from '@/shared/types/api';

export const SALES_CUSTOMER_AGGREGATE_CONTRACT_MESSAGE =
  '영업 고객 집계 응답이 요청한 고객사와 일치하지 않습니다.';

/** 요청한 고객 ID 집합과 응답 집합이 중복 없이 정확히 일치하는지 검증한다. */
export function getSalesCustomerAggregateContractError(
  aggregates: unknown,
  customerIds: number[],
): ApiError | undefined {
  if (!Array.isArray(aggregates) || aggregates.some((aggregate) => !isAggregate(aggregate))) {
    return { status: 500, message: SALES_CUSTOMER_AGGREGATE_CONTRACT_MESSAGE };
  }
  const expectedIds = new Set(customerIds);
  const actualIds = new Set(aggregates.map((aggregate) => aggregate.customerId));
  const hasDuplicateRequest = expectedIds.size !== customerIds.length;
  const hasDuplicateResponse = actualIds.size !== aggregates.length;
  const hasMissingId = customerIds.some((id) => !actualIds.has(id));
  const hasUnexpectedId = aggregates.some((aggregate) => !expectedIds.has(aggregate.customerId));

  if (hasDuplicateRequest || hasDuplicateResponse || hasMissingId || hasUnexpectedId) {
    return { status: 500, message: SALES_CUSTOMER_AGGREGATE_CONTRACT_MESSAGE };
  }

  return undefined;
}

/** API 응답을 예외 없이 RTK Query의 성공/실패 결과로 변환한다. */
export function resolveSalesCustomerAggregateResponse(
  response: unknown,
  customerIds: number[],
): { data: SalesCustomerAggregate[] } | { error: ApiError } {
  const error = getSalesCustomerAggregateContractError(response, customerIds);
  return error
    ? { error }
    : { data: response as SalesCustomerAggregate[] };
}

function isAggregate(value: unknown): value is SalesCustomerAggregate {
  if (typeof value !== 'object' || value === null) return false;
  const aggregate = value as Partial<SalesCustomerAggregate>;
  return Number.isInteger(aggregate.customerId)
    && (aggregate.primaryAssigneeId === null || Number.isInteger(aggregate.primaryAssigneeId))
    && (aggregate.primaryAssigneeName === null || typeof aggregate.primaryAssigneeName === 'string')
    && Number.isInteger(aggregate.activeAssigneeCount)
    && Number.isInteger(aggregate.activityCount)
    && (aggregate.lastActivityDate === null || typeof aggregate.lastActivityDate === 'string');
}
