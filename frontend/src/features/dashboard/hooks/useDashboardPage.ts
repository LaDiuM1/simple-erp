import { useNavigate } from 'react-router-dom';
import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';
import { useGetDashboardSummaryQuery } from '@/features/dashboard/api/dashboardApi';
import { usePermission } from '@/shared/hooks/usePermission';
import { MENU_CODE } from '@/shared/config/menuConfig';
import type { QuickAction } from '../components/GreetingRow/GreetingRow';

/**
 * 대시보드 page hook — 두 query (profile + summary) + 타일별 navigate handler + 빠른 작업.
 */
export function useDashboardPage() {
  const navigate = useNavigate();
  const profileQuery = useGetMyProfileQuery();
  const summaryQuery = useGetDashboardSummaryQuery();
  const { canWrite: canWriteSales } = usePermission(MENU_CODE.SALES_CUSTOMERS);
  const { canWrite: canWriteCustomers } = usePermission(MENU_CODE.CUSTOMERS);

  const quickActions: QuickAction[] = [
    ...(canWriteCustomers
      ? [{ label: '고객사 등록', onClick: () => navigate('/customers/new') }]
      : []),
    ...(canWriteSales
      ? [{ label: '활동 기록', onClick: () => navigate('/sales-customers'), primary: true }]
      : []),
  ];

  return {
    queries: { profile: profileQuery, summary: summaryQuery },
    quickActions,
    onOpenSalesCustomer: (customerId: number) => navigate(`/sales-customers/${customerId}`),
    onNavigateCustomers: () => navigate('/customers'),
    onNavigateSalesContacts: () => navigate('/sales-contacts'),
    onNavigateEmployees: () => navigate('/employees'),
    onNavigateSalesCustomers: () => navigate('/sales-customers'),
  };
}
