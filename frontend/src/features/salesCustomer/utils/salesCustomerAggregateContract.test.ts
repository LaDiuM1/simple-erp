import { describe, expect, it, vi } from 'vitest';
import type { SalesCustomerAggregate } from '@/features/salesCustomer/types';
import {
  getSalesCustomerAggregateContractError,
  resolveSalesCustomerAggregateResponse,
  SALES_CUSTOMER_AGGREGATE_CONTRACT_MESSAGE,
} from './salesCustomerAggregateContract';

const aggregate = (customerId: number): SalesCustomerAggregate => ({
  customerId,
  primaryAssigneeId: null,
  primaryAssigneeName: null,
  activeAssigneeCount: 0,
  activityCount: 0,
  lastActivityDate: null,
});

describe('validateSalesCustomerAggregates', () => {
  it('순서와 무관하게 정확한 고객 ID 집합을 허용한다', () => {
    expect(getSalesCustomerAggregateContractError([aggregate(2), aggregate(1)], [1, 2]))
      .toBeUndefined();
  });

  it.each([
    { name: '누락', response: [aggregate(1)], request: [1, 2] },
    { name: '초과', response: [aggregate(1), aggregate(3)], request: [1] },
    { name: '응답 중복', response: [aggregate(1), aggregate(1)], request: [1] },
    { name: '요청 중복', response: [aggregate(1)], request: [1, 1] },
    { name: '배열 아님', response: null, request: [1] },
    { name: '필드 누락', response: [{ customerId: 1 }], request: [1] },
  ])('$name 응답을 계약 오류로 거부한다', ({ response, request }) => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    expect(getSalesCustomerAggregateContractError(response, request)).toEqual({
      status: 500,
      message: SALES_CUSTOMER_AGGREGATE_CONTRACT_MESSAGE,
    });
    expect(consoleError).not.toHaveBeenCalled();
    consoleError.mockRestore();
  });

  it('계약 위반을 예외나 콘솔 오류 없이 RTK Query 오류 결과로 변환한다', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);

    expect(resolveSalesCustomerAggregateResponse([aggregate(1)], [1, 2])).toEqual({
      error: { status: 500, message: SALES_CUSTOMER_AGGREGATE_CONTRACT_MESSAGE },
    });
    expect(consoleError).not.toHaveBeenCalled();
    consoleError.mockRestore();
  });
});
