import { useNavigate } from 'react-router-dom';
import EventNoteRoundedIcon from '@mui/icons-material/EventNoteRounded';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingSpinner from '@/shared/ui/feedback/LoadingSpinner';
import Muted from '@/shared/ui/atoms/Muted';
import { MENU_PATH, MENU_CODE } from '@/shared/config/menuConfig';
import { useGetSalesActivitiesByContactQuery } from '@/features/salesCustomer/api/salesCustomerApi';
import ActivityTypeBadge from '@/features/salesCustomer/components/ActivityTypeBadge';
import type { ApiError } from '@/shared/types/api';
import {
  EmptySection,
  ItemCard,
  ItemContent,
  ItemHeader,
  ItemHeaderLeft,
  ItemList,
  ItemSubtle,
  ItemTitle,
  ItemTitleLink,
  SectionHeader,
  SectionRoot,
  SectionTitle,
  SectionTitleCount,
} from './salesContactDetail.styles';

interface Props {
  contactId: number;
}

/**
 * 영업 명부 상세에서 보여주는 read-only 활동 이력. 등록/수정/삭제는 고객사 영업 관리 페이지에서.
 * 카드의 고객사명을 클릭하면 해당 고객사 영업 상세로 이동.
 */
export default function ContactActivityList({ contactId }: Props) {
  const navigate = useNavigate();
  const { data, isLoading, isError, error, refetch } = useGetSalesActivitiesByContactQuery(contactId);

  return (
    <SectionRoot>
      <SectionHeader>
        <SectionTitle>
          <EventNoteRoundedIcon sx={{ fontSize: 18, color: 'primary.main' }} />
          영업 활동 이력
          <SectionTitleCount>({data?.length ?? 0})</SectionTitleCount>
        </SectionTitle>
      </SectionHeader>

      {isLoading ? (
        <LoadingSpinner />
      ) : isError ? (
        <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />
      ) : !data || data.length === 0 ? (
        <EmptySection>등록된 영업 활동이 없습니다.</EmptySection>
      ) : (
        <ItemList>
          {data.map((a) => (
            <ItemCard key={a.id}>
              <ItemHeader>
                <ItemHeaderLeft>
                  <ActivityTypeBadge type={a.type} />
                  <ItemTitle>{a.subject}</ItemTitle>
                  <ItemSubtle>{formatDateTime(a.activityDate)}</ItemSubtle>
                </ItemHeaderLeft>
              </ItemHeader>
              <ItemSubtle>
                고객사:{' '}
                {a.customerName ? (
                  <ItemTitleLink
                    type="button"
                    onClick={() => navigate(`${MENU_PATH[MENU_CODE.SALES_CUSTOMERS]}/${a.customerId}`)}
                  >
                    {a.customerName}
                  </ItemTitleLink>
                ) : (
                  <Muted />
                )}
                {' · '}
                우리 담당: {a.ourEmployeeName ?? <Muted />}
                {a.ourEmployeeDepartmentName ? ` (${a.ourEmployeeDepartmentName})` : ''}
              </ItemSubtle>
              {a.content && <ItemContent>{a.content}</ItemContent>}
            </ItemCard>
          ))}
        </ItemList>
      )}
    </SectionRoot>
  );
}

function formatDateTime(iso: string): string {
  return iso.replace('T', ' ').slice(0, 16);
}
