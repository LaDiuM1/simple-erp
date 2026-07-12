import { useNavigate } from 'react-router-dom';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
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
  const profileQuery = useGetMyProfileQuery();
  const summaryQuery = useGetDashboardSummaryQuery();

  const { canRead: canReadContracts } = usePermission(MENU_CODE.CONTRACTS);
  const { canRead: canReadAfterServices } = usePermission(MENU_CODE.AFTER_SERVICES);
  const { canRead: canReadEquipments } = usePermission(MENU_CODE.EQUIPMENTS);

  const salesQuery = useGetDashboardSalesQuery(undefined, { skip: !canReadContracts });
  const serviceQuery = useGetDashboardServiceStatsQuery(undefined, { skip: !canReadAfterServices });
  const warrantyQuery = useGetDashboardWarrantyQuery(undefined, { skip: !canReadEquipments });

  const monthLabel = `${new Date().getMonth() + 1}월`;

  return {
    queries: { profile: profileQuery, summary: summaryQuery },
    widgets: {
      sales: canReadContracts ? salesQuery.data : undefined,
      service: canReadAfterServices ? serviceQuery.data : undefined,
      warranty: canReadEquipments ? warrantyQuery.data : undefined,
    },
    monthLabel,
    onNavigateCustomers: () => navigate('/customers'),
    onNavigateSalesContacts: () => navigate('/sales-contacts'),
    onNavigateEmployees: () => navigate('/employees'),
    onNavigateSalesCustomers: () => navigate('/sales-customers'),
  };
}
