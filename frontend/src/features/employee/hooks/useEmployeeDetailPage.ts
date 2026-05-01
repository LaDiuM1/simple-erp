import { useGetEmployeeQuery } from '@/features/employee/api/employeeApi';

/**
 * 직원 상세 page hook — outer fetch 만 노출.
 * headerActions 는 EmployeeDetailForm 가 자체 렌더 (read-only 폼 톤 유지).
 */
export function useEmployeeDetailPage(id: number) {
  const detailQuery = useGetEmployeeQuery(id, { skip: !id });
  return { queries: { detail: detailQuery } };
}
