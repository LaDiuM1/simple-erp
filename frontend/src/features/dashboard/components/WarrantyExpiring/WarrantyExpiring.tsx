import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import WarrantyDateText from '@/features/equipment/components/WarrantyDateText';
import {
  EmptyState,
  ItemAction,
  ItemList,
  ItemMain,
  ItemMeta,
  ItemRow,
  ItemTime,
  ItemTitle,
  ItemTopLine,
  SectionCount,
  SectionDescription,
  SectionHeader,
  SectionHeading,
  SectionMore,
  SectionRoot,
  SectionTitle,
} from '../DashboardCard.styles';
import type { ExpiringWarrantyItem } from '../../types';

interface Props {
  items: ExpiringWarrantyItem[];
}

type ImminentWarranty = { label: string; endDate: string };

/**
 * 위젯에 표시할 임박 보증 1건 — 일반 (무상 AS) / 발진기 중 아직 만료 전 (오늘 이후) 이면서 가장 이른 만료일.
 * BE 선정 기준 (둘 중 하나라도 90일 내 만료) 과 표시를 일치시켜, 발진기만 임박한 설비에 엉뚱한 (과거 / 비임박)
 * 일반 보증일이 "무상 AS" 로 노출되던 문제를 막는다. BE 가 최소 1건 임박을 보장하므로 정상 데이터엔 항상 값이 있다.
 */
function imminentWarranty(item: ExpiringWarrantyItem): ImminentWarranty | null {
  const candidates: ImminentWarranty[] = [];
  if (item.generalWarrantyEndDate) {
    candidates.push({ label: '무상 AS', endDate: item.generalWarrantyEndDate });
  }
  if (item.oscillatorWarrantyEndDate) {
    candidates.push({ label: '발진기', endDate: item.oscillatorWarrantyEndDate });
  }
  if (candidates.length === 0) return null;

  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const notExpired = candidates.filter((c) => new Date(`${c.endDate}T00:00:00`) >= today);
  const pool = notExpired.length > 0 ? notExpired : candidates;
  // ISO 날짜 문자열은 사전식 정렬이 곧 시간순 -> 가장 이른 (임박한) 만료일 선택
  return pool.reduce((soonest, c) => (c.endDate < soonest.endDate ? c : soonest));
}

/**
 * 보증 만료 임박 설비 (90일 내) — EQUIPMENTS read 권한자 전용 위젯.
 * 만료 전 선제 대응 (보증 연장 영업 / 사전 점검) 용도.
 */
export default function WarrantyExpiring({ items }: Props) {
  return (
    <SectionRoot>
      <SectionHeader>
        <SectionHeading>
          <SectionTitle>
            확인할 보증 일정
            <SectionCount>{items.length}</SectionCount>
          </SectionTitle>
          <SectionDescription>90일 안에 보증이 만료되는 설비예요.</SectionDescription>
        </SectionHeading>
        <SectionMore to="/equipments">
          전체 보기
          <ArrowForwardRoundedIcon sx={{ fontSize: 14 }} />
        </SectionMore>
      </SectionHeader>
      {items.length === 0 ? (
        <EmptyState>90일 내 만료 예정인 보증이 없습니다.</EmptyState>
      ) : (
        <ItemList>
          {items.map((item) => {
            const warranty = imminentWarranty(item);
            return (
              <ItemRow key={item.equipmentId}>
                <ItemAction to={`/equipments/${item.equipmentId}`}>
                  <ItemMain>
                    <ItemTopLine>
                      <ItemTitle>{item.customerName ?? '고객사 미상'}</ItemTitle>
                    </ItemTopLine>
                    <ItemMeta>
                      {item.productModelName ?? '모델 미상'}
                      {item.serialNo ? ` (${item.serialNo})` : ''}
                    </ItemMeta>
                  </ItemMain>
                  <ItemTime>
                    {warranty ? (
                      <>
                        {warranty.label} <WarrantyDateText endDate={warranty.endDate} />
                      </>
                    ) : (
                      <WarrantyDateText endDate={null} />
                    )}
                  </ItemTime>
                </ItemAction>
              </ItemRow>
            );
          })}
        </ItemList>
      )}
    </SectionRoot>
  );
}
