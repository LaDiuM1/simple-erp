import { useNavigate } from 'react-router-dom';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import { useToday } from '@/shared/hooks/useToday';
import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';
import {
  useGetDashboardSalesQuery,
  useGetDashboardServiceStatsQuery,
  useGetDashboardSummaryQuery,
  useGetDashboardWarrantyQuery,
} from '@/features/dashboard/api/dashboardApi';

/**
 * 대시보드 page hook — 공통 query (profile + summary) 는 QueryGate 로 분기하고,
 * 권한별 위젯 (계약 실적 / AS 현황 / 보증 임박) 은 read 권한이 있을 때만 조회해
 * 데이터가 도착한 섹션만 노출 (권한 없는 사용자의 화면을 막지 않는다).
 */
export function useDashboardPage() {
  const navigate = useNavigate();
  const today = useToday();
  const profileQuery = useGetMyProfileQuery();
  const summaryQuery = useGetDashboardSummaryQuery();

  const { canRead: canReadCustomers } = usePermission(MENU_CODE.CUSTOMERS);
  const { canRead: canReadSalesContacts } = usePermission(MENU_CODE.SALES_CONTACTS);
  const { canRead: canReadEmployees } = usePermission(MENU_CODE.EMPLOYEES);
  const { canRead: canReadSalesCustomers } = usePermission(MENU_CODE.SALES_CUSTOMERS);
  const { canRead: canReadContracts } = usePermission(MENU_CODE.CONTRACTS);
  const { canRead: canReadAfterServices } = usePermission(MENU_CODE.AFTER_SERVICES);
  const { canRead: canReadEquipments } = usePermission(MENU_CODE.EQUIPMENTS);

  const salesQuery = useGetDashboardSalesQuery(undefined, { skip: !canReadContracts });
  const serviceQuery = useGetDashboardServiceStatsQuery(undefined, { skip: !canReadAfterServices });
  const warrantyQuery = useGetDashboardWarrantyQuery(undefined, { skip: !canReadEquipments });

  const monthLabel = `${today.getMonth() + 1}월`;

  return {
    queries: {
      profile: profileQuery,
      summary: summaryQuery,
      ...(canReadContracts ? { sales: salesQuery } : {}),
      ...(canReadAfterServices ? { service: serviceQuery } : {}),
      ...(canReadEquipments ? { warranty: warrantyQuery } : {}),
    },
    widgets: {
      sales: canReadContracts ? salesQuery.data : undefined,
      service: canReadAfterServices ? serviceQuery.data : undefined,
      warranty: canReadEquipments ? warrantyQuery.data : undefined,
    },
    monthLabel,
    onNavigateCustomers: canReadCustomers ? () => navigate('/customers') : undefined,
    onNavigateCustomer: canReadCustomers
      ? (customerId: number) => navigate(`/customers/${customerId}`)
      : undefined,
    onNavigateSalesContacts: canReadSalesContacts ? () => navigate('/sales-contacts') : undefined,
    onNavigateEmployees: canReadEmployees ? () => navigate('/employees') : undefined,
    onNavigateSalesCustomers: canReadSalesCustomers
      ? () => navigate('/sales-customers')
      : undefined,
    onNavigateSalesCustomer: canReadSalesCustomers
      ? (customerId: number) => navigate(`/sales-customers/${customerId}`)
      : undefined,
  };
}
