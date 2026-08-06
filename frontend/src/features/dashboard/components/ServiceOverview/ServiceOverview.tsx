import { useNavigate } from 'react-router-dom';
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
  EngineerAmount,
  EngineerList,
  EngineerName,
  EngineerRow,
  ServiceBar,
  ServiceBarFill,
  ServiceCount,
  ServiceExpense,
  ServiceLabel,
  ServiceMetric,
  ServiceMetricLabel,
  ServiceMetricValue,
  ServiceSummary,
  ServiceTypeList,
  ServiceTypeRow,
  SubHeading,
} from './ServiceOverview.styles';
import { ratioPercent } from '../../utils/dashboardMetrics';
import type { DashboardServiceStats } from '../../types';

interface Props {
  data: DashboardServiceStats;
}

/** 최근 6개월 AS 규모, 유형 분포, 비용 상위 담당자를 한 운영 문맥으로 묶는다. */
export default function ServiceOverview({ data }: Props) {
  const navigate = useNavigate();
  const activeTypes = data.typeStats.filter((stat) => stat.count > 0);
  const totalCount = activeTypes.reduce((sum, stat) => sum + stat.count, 0);
  const totalExpense = activeTypes.reduce((sum, stat) => sum + stat.expenseTotal, 0);
  const maxTypeCount = Math.max(...activeTypes.map((stat) => stat.count), 0);
  const topEngineers = data.engineerStats.slice(0, 3);

  return (
    <SectionRoot>
      <SectionHeader>
        <SectionHeading>
          <SectionTitle>AS 운영</SectionTitle>
          <SectionDescription>최근 6개월 접수 유형과 처리 비용이에요.</SectionDescription>
        </SectionHeading>
        <SectionMore type="button" onClick={() => navigate('/after-services')}>
          AS 보기
          <ArrowForwardRoundedIcon sx={{ fontSize: 14 }} />
        </SectionMore>
      </SectionHeader>

      <ServiceSummary aria-label="AS 운영 요약">
        <ServiceMetric>
          <ServiceMetricLabel>총 접수</ServiceMetricLabel>
          <ServiceMetricValue>{totalCount.toLocaleString('ko-KR')}건</ServiceMetricValue>
        </ServiceMetric>
        <ServiceMetric>
          <ServiceMetricLabel>처리 비용</ServiceMetricLabel>
          <ServiceMetricValue>{formatKrw(totalExpense)}</ServiceMetricValue>
        </ServiceMetric>
      </ServiceSummary>

      {activeTypes.length === 0 ? (
        <EmptyState>최근 6개월 AS 접수가 없습니다.</EmptyState>
      ) : (
        <>
          <SubHeading>접수 유형</SubHeading>
          <ServiceTypeList>
            {activeTypes.map((stat) => (
              <ServiceTypeRow key={stat.type} aria-label={`${stat.typeLabel} AS 현황`}>
                <ServiceLabel>{stat.typeLabel}</ServiceLabel>
                <ServiceBar aria-hidden="true">
                  <ServiceBarFill $width={ratioPercent(stat.count, maxTypeCount)} />
                </ServiceBar>
                <ServiceCount>{stat.count}건</ServiceCount>
                <ServiceExpense>
                  {formatKrw(stat.expenseTotal)}
                </ServiceExpense>
              </ServiceTypeRow>
            ))}
          </ServiceTypeList>
        </>
      )}

      {topEngineers.length > 0 && (
        <>
          <SubHeading>비용 상위 담당자</SubHeading>
          <EngineerList>
            {topEngineers.map((engineer) => (
              <EngineerRow key={engineer.engineerId}>
                <EngineerName>
                  {engineer.engineerName ?? `엔지니어 #${engineer.engineerId}`}
                </EngineerName>
                <EngineerAmount>{formatKrw(engineer.expenseTotal)}</EngineerAmount>
              </EngineerRow>
            ))}
          </EngineerList>
        </>
      )}
    </SectionRoot>
  );
}
