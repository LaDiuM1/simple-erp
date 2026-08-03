import type { SalesAssignment } from '@/features/salesCustomer/types';

/** 담당자 신규 배정 후보에서 제외할 현재 활성 직원 id 집합. */
export function activeAssignmentEmployeeIds(assignments: SalesAssignment[]): number[] {
  return [...new Set(
    assignments
      .filter((assignment) => assignment.active)
      .map((assignment) => assignment.employeeId),
  )];
}
