import { useGetEmployeeQuery } from '@/features/employee/api/employeeApi';

/**
 * 직원 수정 page hook — outer fetch 만 노출.
 * headerActions 는 form-state 에 의존하므로 EmployeeEditForm Body 안에서 렌더.
 */
export function useEmployeeEditPage(id: number) {
  const detailQuery = useGetEmployeeQuery(id, { skip: !id });
  return { queries: { detail: detailQuery } };
}
