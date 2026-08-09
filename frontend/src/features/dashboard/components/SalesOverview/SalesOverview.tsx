import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import { formatKrw } from '@/shared/utils/formatKrw';
import {
  EmptyState,
  SectionDescription,
  SectionHeader,
  SectionHeading,
  SectionMore,
  SectionRoot,
  SectionTitle,
} from '../DashboardCard.styles';
import {
  CollectionHeader,
  CollectionLabel,
  CollectionPanel,
  CollectionRate,
  CollectionValue,
  CollectionValues,
  MetricLabel,
  MetricValue,
  PerformanceMetric,
  PerformanceSummary,
  ProgressFill,
  ProgressTrack,
  TrendAmount,
  TrendBar,
  TrendBarFill,
  TrendCount,
  TrendHeader,
  TrendList,
  TrendMonth,
  TrendRow,
} from './SalesOverview.styles';
import { ratioPercent } from '../../utils/dashboardMetrics';
import type { DashboardSales } from '../../types';

interface Props {
  data: DashboardSales;
}

/** "2026-01" → "26년 1월" */
function monthLabel(month: string): string {
  const [year, monthNumber] = month.split('-');
  return `${year.slice(2)}년 ${Number(monthNumber)}월`;
}

/** 권한 범위 안의 월별 계약 추이와 수금 상태를 한 흐름으로 보여준다. */
export default function SalesOverview({ data }: Props) {
  const totalCount = data.monthlyStats.reduce((sum, stat) => sum + stat.count, 0);
  const sixMonthAmount = data.monthlyStats.reduce((sum, stat) => sum + stat.totalAmount, 0);
  const maxMonthlyAmount = Math.max(...data.monthlyStats.map((stat) => stat.totalAmount), 0);
  const collectionRate = ratioPercent(
    data.outstanding.totalPaidAmount,
    data.outstanding.totalFinalAmount,
  );

  return (
    <SectionRoot>
      <SectionHeader>
        <SectionHeading>
          <SectionTitle>계약과 수금</SectionTitle>
          <SectionDescription>최근 6개월 계약 추이와 현재 수금 상태예요.</SectionDescription>
        </SectionHeading>
        <SectionMore to="/contracts">
          계약 보기
          <ArrowForwardRoundedIcon sx={{ fontSize: 14 }} />
        </SectionMore>
      </SectionHeader>

      <PerformanceSummary>
        <PerformanceMetric>
          <MetricLabel>6개월 계약액</MetricLabel>
          <MetricValue>{formatKrw(sixMonthAmount)}</MetricValue>
        </PerformanceMetric>
        <PerformanceMetric>
          <MetricLabel>6개월 계약 건수</MetricLabel>
          <MetricValue>{totalCount.toLocaleString('ko-KR')}건</MetricValue>
        </PerformanceMetric>
      </PerformanceSummary>

      {data.monthlyStats.length === 0 ? (
        <EmptyState>최근 6개월 계약 실적이 없습니다.</EmptyState>
      ) : (
        <>
          <TrendHeader>
            <span>월별 계약액</span>
            <span>금액</span>
          </TrendHeader>
          <TrendList aria-label="최근 6개월 계약액 추이">
            {data.monthlyStats.map((stat) => (
              <TrendRow key={stat.month}>
                <TrendMonth>{monthLabel(stat.month)}</TrendMonth>
                <TrendBar aria-hidden="true">
                  <TrendBarFill $width={ratioPercent(stat.totalAmount, maxMonthlyAmount)} />
                </TrendBar>
                <TrendAmount>{formatKrw(stat.totalAmount)}</TrendAmount>
                <TrendCount>{stat.count}건</TrendCount>
              </TrendRow>
            ))}
          </TrendList>
        </>
      )}

      <CollectionPanel>
        <CollectionHeader>
          <CollectionLabel>수금 진행률</CollectionLabel>
          <CollectionRate>{collectionRate}%</CollectionRate>
        </CollectionHeader>
        <ProgressTrack
          role="progressbar"
          aria-label="수금 진행률"
          aria-valuemin={0}
          aria-valuemax={100}
          aria-valuenow={collectionRate}
        >
          <ProgressFill $width={collectionRate} />
        </ProgressTrack>
        <CollectionValues>
          <CollectionValue>
            <span>계약액</span>
            <strong>{formatKrw(data.outstanding.totalFinalAmount)}</strong>
          </CollectionValue>
          <CollectionValue>
            <span>수금</span>
            <strong>{formatKrw(data.outstanding.totalPaidAmount)}</strong>
          </CollectionValue>
          <CollectionValue $warning={data.outstanding.totalOutstandingAmount > 0}>
            <span>미수</span>
            <strong>{formatKrw(data.outstanding.totalOutstandingAmount)}</strong>
          </CollectionValue>
        </CollectionValues>
      </CollectionPanel>
    </SectionRoot>
  );
}
