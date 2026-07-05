import QueryGate from '@/shared/ui/feedback/QueryGate';
import GreetingRow from '@/features/dashboard/components/GreetingRow/GreetingRow';
import TrendHeroCard from '@/features/dashboard/components/TrendHeroCard/TrendHeroCard';
import StatTile from '@/features/dashboard/components/StatTile/StatTile';
import FollowUpCard from '@/features/dashboard/components/FollowUpCard/FollowUpCard';
import TimelineCard from '@/features/dashboard/components/TimelineCard/TimelineCard';
import MiniStatTile from '@/features/dashboard/components/MiniStatTile/MiniStatTile';
import { useDashboardPage } from '@/features/dashboard/hooks/useDashboardPage';
import {
  BentoGrid,
  DashboardRoot,
  FollowUpArea,
  HeroArea,
  MiniColumn,
  StatsColumn,
  TimelineArea,
} from './DashboardPage.styles';

export default function DashboardPage() {
  const {
    queries,
    quickActions,
    onOpenSalesCustomer,
    onNavigateCustomers,
    onNavigateSalesContacts,
    onNavigateEmployees,
    onNavigateSalesCustomers,
  } = useDashboardPage();

  return (
    <QueryGate queries={queries}>
      {({ profile, summary }) => (
        <DashboardRoot>
          <GreetingRow
            profile={profile}
            followUpCount={summary.followUps.length}
            quickActions={quickActions}
          />

          <BentoGrid>
            <HeroArea>
              <TrendHeroCard
                trend={summary.weeklyActivityTrend}
                monthlyTotal={summary.kpi.monthlySalesActivities}
              />
            </HeroArea>

            <StatsColumn>
              <StatTile
                label="총 고객사"
                value={summary.kpi.totalCustomers}
                unit="개사"
                delta={{ value: summary.newCustomersThisWeek, periodLabel: '이번 주' }}
                onClick={onNavigateCustomers}
              />
              <StatTile
                label="영업 명부"
                value={summary.kpi.totalSalesContacts}
                unit="명"
                delta={{ value: summary.newSalesContactsThisMonth, periodLabel: '이번 달' }}
                onClick={onNavigateSalesContacts}
              />
              <StatTile
                label="재직 직원"
                value={summary.kpi.activeEmployees}
                unit="명"
                onClick={onNavigateEmployees}
              />
            </StatsColumn>

            <FollowUpArea>
              <FollowUpCard
                items={summary.followUps}
                onItemClick={onOpenSalesCustomer}
                onMore={onNavigateSalesCustomers}
              />
            </FollowUpArea>

            <TimelineArea>
              <TimelineCard
                items={summary.recentActivities}
                onItemClick={onOpenSalesCustomer}
                onMore={onNavigateSalesCustomers}
              />
            </TimelineArea>

            <MiniColumn>
              <MiniStatTile
                label="이번 주 신규 명부"
                value={`+${summary.newSalesContactsThisWeek.toLocaleString()}`}
                unit="명"
                accent
              />
              <MiniStatTile
                label="이번 주 신규 고객사"
                value={`+${summary.newCustomersThisWeek.toLocaleString()}`}
                unit="개사"
                accent
              />
              <MiniStatTile
                label="이번 달 미접촉 고객"
                value={summary.uncontactedCustomersThisMonth.toLocaleString()}
                unit="개사"
              />
            </MiniColumn>
          </BentoGrid>
        </DashboardRoot>
      )}
    </QueryGate>
  );
}
