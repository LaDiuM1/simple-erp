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
  SubTitle,
} from '../statRow.styles';
import type { DashboardServiceStats } from '../../types';

interface Props {
  data: DashboardServiceStats;
}

/**
 * AS 유형별 건수 / 원가 + 엔지니어별 원가 — AFTER_SERVICES read 권한자 전용 위젯 (최근 6개월).
 */
export default function ServiceOverview({ data }: Props) {
  const navigate = useNavigate();
  const hasAny = data.typeStats.some((s) => s.count > 0);

  return (
    <SectionRoot>
      <SectionHeader>
        <SectionTitle>AS 현황 (최근 6개월)</SectionTitle>
        <SectionMore type="button" onClick={() => navigate('/after-services')}>
          전체 보기
          <ArrowForwardRoundedIcon sx={{ fontSize: 14 }} />
        </SectionMore>
      </SectionHeader>
      {!hasAny ? (
        <EmptyState>최근 6개월 AS 접수가 없습니다.</EmptyState>
      ) : (
        <>
          <ItemList>
            {data.typeStats.map((s) => (
              <StatRow key={s.type}>
                <StatLabel>{s.typeLabel}</StatLabel>
                <StatCount>{s.count}건</StatCount>
                <StatAmount>{formatKrw(s.expenseTotal)}</StatAmount>
              </StatRow>
            ))}
          </ItemList>
          {data.engineerStats.length > 0 && (
            <>
              <SubTitle>엔지니어별 원가</SubTitle>
              <ItemList>
                {data.engineerStats.map((s) => (
                  <StatRow key={s.engineerId}>
                    <StatLabel>{s.engineerName ?? `엔지니어 #${s.engineerId}`}</StatLabel>
                    <StatAmount>{formatKrw(s.expenseTotal)}</StatAmount>
                  </StatRow>
                ))}
              </ItemList>
            </>
          )}
        </>
      )}
    </SectionRoot>
  );
}
