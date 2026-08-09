import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import BusinessRoundedIcon from '@mui/icons-material/BusinessRounded';
import ContactsRoundedIcon from '@mui/icons-material/ContactsRounded';
import TrendingUpRoundedIcon from '@mui/icons-material/TrendingUpRounded';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import HeroBanner from '@/features/dashboard/components/HeroBanner/HeroBanner';
import KpiCard from '@/features/dashboard/components/KpiCard/KpiCard';
import RecentCustomers from '@/features/dashboard/components/RecentCustomers/RecentCustomers';
import RecentActivities from '@/features/dashboard/components/RecentActivities/RecentActivities';
import SalesOverview from '@/features/dashboard/components/SalesOverview/SalesOverview';
import ServiceOverview from '@/features/dashboard/components/ServiceOverview/ServiceOverview';
import WarrantyExpiring from '@/features/dashboard/components/WarrantyExpiring/WarrantyExpiring';
import DashboardSectionHeading from '@/features/dashboard/components/DashboardSectionHeading/DashboardSectionHeading';
import { useDashboardPage } from '@/features/dashboard/hooks/useDashboardPage';
import DemoExperienceGuide from '@/features/demo/components/DemoExperienceGuide';
import {
  DashboardGroup,
  DashboardRoot,
  DemoGuideRegion,
  KpiGrid,
  OperationsGrid,
  OperationsRail,
  RecentGrid,
} from './DashboardPage.styles';

export default function DashboardPage() {
  const {
    queries,
    widgets,
    monthLabel,
    customerListPath,
    customerDetailPath,
    salesContactListPath,
    employeeListPath,
    salesCustomerListPath,
    salesCustomerDetailPath,
  } = useDashboardPage();

  return (
    <QueryGate queries={queries}>
      {({ profile, summary }) => (
        <DashboardContent
          profile={profile}
          summary={summary}
          widgets={widgets}
          monthLabel={monthLabel}
          customerListPath={customerListPath}
          customerDetailPath={customerDetailPath}
          salesContactListPath={salesContactListPath}
          employeeListPath={employeeListPath}
          salesCustomerListPath={salesCustomerListPath}
          salesCustomerDetailPath={salesCustomerDetailPath}
        />
      )}
    </QueryGate>
  );
}

type DashboardContentProps = Omit<ReturnType<typeof useDashboardPage>, 'queries'> & {
  profile: Parameters<typeof HeroBanner>[0]['profile'];
  summary: NonNullable<ReturnType<typeof useDashboardPage>['queries']['summary']['data']>;
};

function DashboardContent({
  profile,
  summary,
  widgets,
  monthLabel,
  customerListPath,
  customerDetailPath,
  salesContactListPath,
  employeeListPath,
  salesCustomerListPath,
  salesCustomerDetailPath,
}: DashboardContentProps) {
  const hasOverview = Object.values(summary.kpi).some((value) => value !== undefined);
  const hasRecent = summary.recentCustomers !== undefined || summary.recentActivities !== undefined;
  const hasWidgets = widgets.sales || widgets.service || widgets.warranty;
  const hasOperationsRail = widgets.service || widgets.warranty;

  return (
    <DashboardRoot>
          <HeroBanner profile={profile} />

          {hasOverview && <DashboardGroup aria-labelledby="dashboard-overview-heading">
            <DashboardSectionHeading
              id="dashboard-overview-heading"
              title="한눈에 보기"
              description="현재 조회 권한 범위에서 집계한 핵심 업무 지표예요."
            />
            <KpiGrid>
              {summary.kpi.totalCustomers !== undefined && <KpiCard
                label="관리 고객사"
                value={summary.kpi.totalCustomers}
                unit="개사"
                icon={<BusinessRoundedIcon />}
                to={customerListPath}
              />}
              {summary.kpi.totalSalesContacts !== undefined && <KpiCard
                label="고객 담당자"
                value={summary.kpi.totalSalesContacts}
                unit="명"
                icon={<ContactsRoundedIcon />}
                to={salesContactListPath}
              />}
              {summary.kpi.activeEmployees !== undefined && <KpiCard
                label="재직 인원"
                value={summary.kpi.activeEmployees}
                unit="명"
                icon={<GroupsRoundedIcon />}
                to={employeeListPath}
              />}
              {summary.kpi.monthlySalesActivities !== undefined && <KpiCard
                label={`${monthLabel} 영업 활동`}
                value={summary.kpi.monthlySalesActivities}
                unit="건"
                suffix="이번 달 누적"
                icon={<TrendingUpRoundedIcon />}
                to={salesCustomerListPath}
              />}
            </KpiGrid>
          </DashboardGroup>}

          {hasWidgets && (
            <DashboardGroup aria-labelledby="dashboard-operations-heading">
              <DashboardSectionHeading
                id="dashboard-operations-heading"
                title="운영 흐름"
                description="매출 흐름과 서비스 일정을 함께 확인하고 다음 업무로 이동할 수 있어요."
              />
              <OperationsGrid>
                {widgets.sales && <SalesOverview data={widgets.sales} />}
                {hasOperationsRail && (
                  <OperationsRail>
                    {widgets.service && <ServiceOverview data={widgets.service} />}
                    {widgets.warranty && <WarrantyExpiring items={widgets.warranty} />}
                  </OperationsRail>
                )}
              </OperationsGrid>
            </DashboardGroup>
          )}

          {hasRecent && <DashboardGroup aria-labelledby="dashboard-recent-heading">
            <DashboardSectionHeading
              id="dashboard-recent-heading"
              title="최근 변화"
              description="새로 등록된 고객사와 최신 영업 활동을 시간순으로 확인해요."
            />
            <RecentGrid>
              {summary.recentCustomers !== undefined && <RecentCustomers
                items={summary.recentCustomers}
                listPath={customerListPath}
                detailPath={customerDetailPath}
              />}
              {summary.recentActivities !== undefined && <RecentActivities
                items={summary.recentActivities}
                listPath={salesCustomerListPath}
                detailPath={salesCustomerDetailPath}
              />}
            </RecentGrid>
          </DashboardGroup>}

      <DemoGuideRegion>
        <DemoExperienceGuide profile={profile} />
      </DemoGuideRegion>
    </DashboardRoot>
  );
}
