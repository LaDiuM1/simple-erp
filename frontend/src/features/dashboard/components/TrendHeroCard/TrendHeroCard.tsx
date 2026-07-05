import type { WeeklyActivityCount } from '../../types';
import { formatShortDate } from '../../utils/formatters';
import {
  Bar,
  BarColumn,
  BarLabel,
  BarTrack,
  ChartArea,
  TrendCaption,
  TrendDelta,
  TrendHead,
  TrendLabel,
  TrendRoot,
  TrendUnit,
  TrendValue,
  TrendValueRow,
} from './TrendHeroCard.styles';

interface Props {
  trend: WeeklyActivityCount[];
  monthlyTotal: number;
}

/**
 * 대시보드 히어로 — 주간 영업 활동 추이 바 차트.
 * 이번 주(마지막 버킷)만 풀 코발트, 나머지는 옅은 톤으로 시선을 현재에 고정.
 */
export default function TrendHeroCard({ trend, monthlyTotal }: Props) {
  const current = trend.at(-1)?.count ?? 0;
  const previous = trend.at(-2)?.count ?? 0;
  const deltaPercent =
    previous > 0 ? Math.round(((current - previous) / previous) * 1000) / 10 : null;
  const max = Math.max(...trend.map((w) => w.count), 1);

  return (
    <TrendRoot>
      <TrendHead>
        <div>
          <TrendLabel>영업 활동 추이</TrendLabel>
          <TrendValueRow>
            <TrendValue>{current.toLocaleString()}</TrendValue>
            <TrendUnit>건 · 이번 주</TrendUnit>
            {deltaPercent !== null && deltaPercent !== 0 && (
              <TrendDelta negative={deltaPercent < 0}>
                {deltaPercent > 0 ? '▲' : '▼'} {Math.abs(deltaPercent)}% 전주 대비
              </TrendDelta>
            )}
          </TrendValueRow>
        </div>
        <TrendCaption>
          최근 8주 · 이번 달 {monthlyTotal.toLocaleString()}건
        </TrendCaption>
      </TrendHead>
      <ChartArea>
        {trend.map((week, index) => {
          const isCurrent = index === trend.length - 1;
          return (
            <BarColumn key={week.weekStart}>
              <BarTrack>
                <Bar
                  current={isCurrent}
                  sx={{ height: `${Math.max((week.count / max) * 100, 2)}%` }}
                  title={`${formatShortDate(week.weekStart)} 주 · ${week.count}건`}
                />
              </BarTrack>
              <BarLabel>{formatShortDate(week.weekStart)}</BarLabel>
            </BarColumn>
          );
        })}
      </ChartArea>
    </TrendRoot>
  );
}
