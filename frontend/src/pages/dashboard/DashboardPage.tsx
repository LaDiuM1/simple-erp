import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import BusinessRoundedIcon from '@mui/icons-material/BusinessRounded';
import ContactsRoundedIcon from '@mui/icons-material/ContactsRounded';
import TrendingUpRoundedIcon from '@mui/icons-material/TrendingUpRounded';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import HeroBanner from '@/features/dashboard/components/HeroBanner/HeroBanner';
import KpiCard from '@/features/dashboard/components/KpiCard/KpiCard';
import RecentCustomers from '@/features/dashboard/components/RecentCustomers/RecentCustomers';
import RecentActivities from '@/features/dashboard/components/RecentActivities/RecentActivities';
import { useDashboardPage } from '@/features/dashboard/hooks/useDashboardPage';
import { DashboardRoot, KpiGrid, RecentGrid } from './DashboardPage.styles';

export default function DashboardPage() {
  const {
    queries,
    monthLabel,
    onNavigateCustomers,
    onNavigateSalesContacts,
    onNavigateEmployees,
    onNavigateSalesCustomers,
  } = useDashboardPage();

  return (
    <QueryGate queries={queries}>
      {({ profile, summary }) => (
        <DashboardRoot>
          <HeroBanner profile={profile} />

          <KpiGrid>
            <KpiCard
              label="총 고객사"
              value={summary.kpi.totalCustomers}
              unit="개사"
              icon={<BusinessRoundedIcon />}
              onClick={onNavigateCustomers}
            />
            <KpiCard
              label="영업 명부"
              value={summary.kpi.totalSalesContacts}
              unit="명"
              icon={<ContactsRoundedIcon />}
              onClick={onNavigateSalesContacts}
            />
            <KpiCard
              label="재직 직원"
              value={summary.kpi.activeEmployees}
              unit="명"
              icon={<GroupsRoundedIcon />}
              onClick={onNavigateEmployees}
            />
            <KpiCard
              label={`${monthLabel} 영업 활동`}
              value={summary.kpi.monthlySalesActivities}
              unit="건"
              suffix="이번 달 누적"
              icon={<TrendingUpRoundedIcon />}
              onClick={onNavigateSalesCustomers}
            />
          </KpiGrid>

          <RecentGrid>
            <RecentCustomers items={summary.recentCustomers} />
            <RecentActivities items={summary.recentActivities} />
          </RecentGrid>
        </DashboardRoot>
      )}
    </QueryGate>
  );
}
