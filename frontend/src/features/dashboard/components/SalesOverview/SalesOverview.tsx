import { useNavigate } from 'react-router-dom';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import { formatKrw } from '@/shared/utils/formatKrw';
import {
  EmptyState,
  ItemList,
  SectionHeader,
  SectionMore,
  SectionRoot,
  SectionTitle,
} from '../RecentSection.styles';
import {
  StatAmount,
  StatCount,
  StatLabel,
  StatRow,
  SummaryFooter,
  SummaryItem,
  SummaryLabel,
  SummaryValue,
} from '../statRow.styles';
import type { DashboardSales } from '../../types';

interface Props {
  data: DashboardSales;
}

/** "2026-01" → "26년 1월" */
function monthLabel(month: string): string {
  const [year, m] = month.split('-');
  return `${year.slice(2)}년 ${Number(m)}월`;
}

/**
 * 월별 계약 실적 + 수금 vs 미수 — CONTRACTS read 권한자 전용 위젯.
 * 금액은 호출자의 데이터 스코프가 적용된 값 (본인 / 부서 범위 사용자는 그 범위의 합계만 보임).
 */
export default function SalesOverview({ data }: Props) {
  const navigate = useNavigate();
  const hasAny = data.monthlyStats.some((s) => s.count > 0);

  return (
    <SectionRoot>
      <SectionHeader>
        <SectionTitle>계약 실적 (최근 6개월)</SectionTitle>
        <SectionMore type="button" onClick={() => navigate('/contracts')}>
          전체 보기
          <ArrowForwardRoundedIcon sx={{ fontSize: 14 }} />
        </SectionMore>
      </SectionHeader>
      {!hasAny ? (
        <EmptyState>최근 6개월 계약 실적이 없습니다.</EmptyState>
      ) : (
        <ItemList>
          {data.monthlyStats.map((s) => (
            <StatRow key={s.month}>
              <StatLabel>{monthLabel(s.month)}</StatLabel>
              <StatCount>{s.count}건</StatCount>
              <StatAmount>{formatKrw(s.totalAmount)}</StatAmount>
            </StatRow>
          ))}
        </ItemList>
      )}
      <SummaryFooter>
        <SummaryItem>
          <SummaryLabel>총 계약액</SummaryLabel>
          <SummaryValue>{formatKrw(data.outstanding.totalFinalAmount)}</SummaryValue>
        </SummaryItem>
        <SummaryItem>
          <SummaryLabel>수금</SummaryLabel>
          <SummaryValue>{formatKrw(data.outstanding.totalPaidAmount)}</SummaryValue>
        </SummaryItem>
        <SummaryItem>
          <SummaryLabel>미수</SummaryLabel>
          <SummaryValue warning={data.outstanding.totalOutstandingAmount > 0}>
            {formatKrw(data.outstanding.totalOutstandingAmount)}
          </SummaryValue>
        </SummaryItem>
      </SummaryFooter>
    </SectionRoot>
  );
}
