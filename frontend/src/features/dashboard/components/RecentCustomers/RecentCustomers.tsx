import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import CustomerStatusIndicator from '@/features/customer/components/CustomerStatusIndicator';
import {
  EmptyState,
  ItemAction,
  ItemContent,
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
import { CodeText, MetaSeparator, TypeText } from './RecentCustomers.styles';
import { CUSTOMER_TYPE_LABELS, type RecentCustomer } from '../../types';
import { formatRelativeTime } from '../../utils/formatters';

interface Props {
  items: RecentCustomer[];
  onNavigateList?: () => void;
  onNavigateCustomer?: (customerId: number) => void;
}

export default function RecentCustomers({ items, onNavigateList, onNavigateCustomer }: Props) {
  return (
    <SectionRoot>
      <SectionHeader>
        <SectionHeading>
          <SectionTitle>
            최근 등록 고객사
            <SectionCount>{items.length}</SectionCount>
          </SectionTitle>
          <SectionDescription>새로 추가된 고객 관계를 빠르게 확인해요.</SectionDescription>
        </SectionHeading>
        {onNavigateList && (
          <SectionMore type="button" onClick={onNavigateList}>
            전체 보기
            <ArrowForwardRoundedIcon sx={{ fontSize: 14 }} />
          </SectionMore>
        )}
      </SectionHeader>
      {items.length === 0 ? (
        <EmptyState>등록된 고객사가 없습니다.</EmptyState>
      ) : (
        <ItemList>
          {items.map((c) => {
            const content = (
              <>
                <ItemMain>
                  <ItemTopLine>
                    <ItemTitle>{c.name}</ItemTitle>
                  </ItemTopLine>
                  <ItemMeta>
                    <CodeText>{c.code}</CodeText>
                    <MetaSeparator />
                    <TypeText>{CUSTOMER_TYPE_LABELS[c.type]}</TypeText>
                    <MetaSeparator />
                    <CustomerStatusIndicator status={c.status} />
                  </ItemMeta>
                </ItemMain>
                <ItemTime>{formatRelativeTime(c.createdAt)}</ItemTime>
              </>
            );

            return (
              <ItemRow key={c.id}>
                {onNavigateCustomer ? (
                  <ItemAction type="button" onClick={() => onNavigateCustomer(c.id)}>
                    {content}
                  </ItemAction>
                ) : (
                  <ItemContent>{content}</ItemContent>
                )}
              </ItemRow>
            );
          })}
        </ItemList>
      )}
    </SectionRoot>
  );
}
