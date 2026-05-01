import { useNavigate } from 'react-router-dom';
import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';
import { useGetDashboardSummaryQuery } from '@/features/dashboard/api/dashboardApi';

/**
 * 대시보드 page hook — 두 query (profile + summary) + KPI 카드별 navigate handler.
 */
export function useDashboardPage() {
  const navigate = useNavigate();
  const profileQuery = useGetMyProfileQuery();
  const summaryQuery = useGetDashboardSummaryQuery();

  const monthLabel = `${new Date().getMonth() + 1}월`;

  return {
    queries: { profile: profileQuery, summary: summaryQuery },
    monthLabel,
    onNavigateCustomers: () => navigate('/customers'),
    onNavigateSalesContacts: () => navigate('/sales-contacts'),
    onNavigateEmployees: () => navigate('/employees'),
    onNavigateSalesCustomers: () => navigate('/sales-customers'),
  };
}
