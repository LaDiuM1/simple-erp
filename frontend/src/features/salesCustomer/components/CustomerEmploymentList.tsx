import PeopleAltRoundedIcon from '@mui/icons-material/PeopleAltRounded';
import { Link as RouterLink } from 'react-router-dom';
import Link from '@mui/material/Link';
import Muted from '@/shared/ui/atoms/Muted';
import { useGetSalesContactEmploymentsByCustomerIdQuery } from '@/features/salesContact/api/salesContactApi';
import EmploymentStatusIndicator from '@/features/salesContact/components/EmploymentStatusIndicator';
import {
  EmptySection,
  ItemCard,
  ItemHeader,
  ItemHeaderLeft,
  ItemList,
  ItemSubtle,
  ItemTitle,
  SectionHeader,
  SectionRoot,
  SectionTitle,
  SectionTitleCount,
} from '@/features/salesCustomer/components/salesCustomerDetail.styles';

/**
 * 고객사 영업 상세 페이지에 노출되는 "고객사 소속 영업 명부" 섹션 — 활성 + 종료된 재직 이력 시간순.
 * 명부 행 클릭으로 명부 상세 페이지 이동.
 */
export default function CustomerEmploymentList({ customerId }: { customerId: number }) {
  const { data = [] } = useGetSalesContactEmploymentsByCustomerIdQuery(customerId);
  const activeCount = data.filter((e) => e.active).length;

  return (
    <SectionRoot>
      <SectionHeader>
        <SectionTitle>
          <PeopleAltRoundedIcon sx={{ fontSize: 18, color: 'primary.main' }} />
          소속 영업 명부
          <SectionTitleCount>(현재 {activeCount} / 전체 {data.length})</SectionTitleCount>
        </SectionTitle>
      </SectionHeader>

      {data.length === 0 ? (
        <EmptySection>이 고객사 소속으로 등록된 영업 명부가 없습니다. 명부 상세에서 재직 등록으로 추가하세요.</EmptySection>
      ) : (
        <ItemList>
          {data.map((e) => (
            <ItemCard key={e.id} sx={{ opacity: e.active ? 1 : 0.7 }}>
              <ItemHeader>
                <ItemHeaderLeft>
                  <EmploymentStatusIndicator
                    active={e.active}
                    endDate={e.endDate}
                    departureType={e.departureType}
                  />
                  <ItemTitle>
                    <Link
                      component={RouterLink}
                      to={`/sales-contacts/${e.contactId}`}
                      underline="hover"
                      sx={{ color: 'inherit' }}
                    >
                      {e.contactName ?? <Muted />}
                    </Link>
                  </ItemTitle>
                  <ItemSubtle>
                    {[e.position, e.department].filter(Boolean).join(' · ')}
                  </ItemSubtle>
                </ItemHeaderLeft>
              </ItemHeader>
              <ItemSubtle>
                기간: {e.startDate} ~ {e.endDate ?? '현재'}
                {e.departureNote ? ` · ${e.departureNote}` : ''}
              </ItemSubtle>
            </ItemCard>
          ))}
        </ItemList>
      )}
    </SectionRoot>
  );
}
