import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';

/**
 * 내 정보 page hook — profile query 만 노출. read-only 페이지.
 */
export function useEmployeeMePage() {
  const profileQuery = useGetMyProfileQuery();
  return { queries: { profile: profileQuery } };
}
