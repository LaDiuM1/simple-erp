import { describe, expect, it } from 'vitest';
import type { SalesAssignment } from '@/features/salesCustomer/types';
import { activeAssignmentEmployeeIds } from './assignmentOptions';

function assignment(employeeId: number, active: boolean): SalesAssignment {
  return {
    id: employeeId,
    customerId: 48,
    employeeId,
    employeeName: `직원 ${employeeId}`,
    employeeDepartmentName: '영업팀',
    employeePositionName: '대리',
    startDate: '2026-08-29',
    endDate: active ? null : '2026-08-28',
    primary: false,
    active,
    reason: null,
  };
}

describe('activeAssignmentEmployeeIds', () => {
  it('현재 활성 배정된 직원만 중복 없이 선택 제외 id 로 변환한다', () => {
    expect(activeAssignmentEmployeeIds([
      assignment(2, true),
      assignment(3, false),
      { ...assignment(2, true), id: 22 },
    ])).toEqual([2]);
  });
});
