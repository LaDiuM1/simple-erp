import { useNavigate } from 'react-router-dom';
import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import BusinessRoundedIcon from '@mui/icons-material/BusinessRounded';
import ContactsRoundedIcon from '@mui/icons-material/ContactsRounded';
import TrendingUpRoundedIcon from '@mui/icons-material/TrendingUpRounded';
import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';
import { useGetDashboardSummaryQuery } from '@/features/dashboard/api/dashboardApi';
import HeroBanner from '@/features/dashboard/components/HeroBanner/HeroBanner';
import KpiCard from '@/features/dashboard/components/KpiCard/KpiCard';
import RecentCustomers from '@/features/dashboard/components/RecentCustomers/RecentCustomers';
import RecentActivities from '@/features/dashboard/components/RecentActivities/RecentActivities';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import { getErrorMessage } from '@/shared/api/error';
import { DashboardRoot, KpiGrid, RecentGrid } from './DashboardPage.styles';

export default function DashboardPage() {
  const navigate = useNavigate();
  const profileQuery = useGetMyProfileQuery();
  const summaryQuery = useGetDashboardSummaryQuery();

  if (profileQuery.isLoading || summaryQuery.isLoading) return <LoadingScreen />;
  if (profileQuery.isError) {
    return <ErrorScreen message={getErrorMessage(profileQuery.error)} onRetry={profileQuery.refetch} />;
  }
  if (summaryQuery.isError) {
    return <ErrorScreen message={getErrorMessage(summaryQuery.error)} onRetry={summaryQuery.refetch} />;
  }
  if (!profileQuery.data || !summaryQuery.data) return null;

  const { kpi, recentCustomers, recentActivities } = summaryQuery.data;
  const monthLabel = `${new Date().getMonth() + 1}월`;

  return (
    <DashboardRoot>
      <HeroBanner profile={profileQuery.data} />

      <KpiGrid>
        <KpiCard
          label="총 고객사"
          value={kpi.totalCustomers}
          unit="개사"
          icon={<BusinessRoundedIcon />}
          onClick={() => navigate('/customers')}
        />
        <KpiCard
          label="영업 명부"
          value={kpi.totalSalesContacts}
          unit="명"
          icon={<ContactsRoundedIcon />}
          onClick={() => navigate('/sales-contacts')}
        />
        <KpiCard
          label="재직 직원"
          value={kpi.activeEmployees}
          unit="명"
          icon={<GroupsRoundedIcon />}
          onClick={() => navigate('/employees')}
        />
        <KpiCard
          label={`${monthLabel} 영업 활동`}
          value={kpi.monthlySalesActivities}
          unit="건"
          suffix="이번 달 누적"
          icon={<TrendingUpRoundedIcon />}
          onClick={() => navigate('/sales-customers')}
        />
      </KpiGrid>

      <RecentGrid>
        <RecentCustomers items={recentCustomers} />
        <RecentActivities items={recentActivities} />
      </RecentGrid>
    </DashboardRoot>
  );
}
