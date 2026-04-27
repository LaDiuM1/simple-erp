import { useNavigate } from 'react-router-dom';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import CustomerStatusIndicator from '@/features/customer/components/CustomerStatusIndicator';
import {
  EmptyState,
  ItemList,
  ItemMain,
  ItemMeta,
  ItemRow,
  ItemTime,
  ItemTitle,
  ItemTopLine,
  SectionHeader,
  SectionMore,
  SectionRoot,
  SectionTitle,
} from '../RecentSection.styles';
import { CodeText, MetaSeparator, TypeText } from './RecentCustomers.styles';
import { CUSTOMER_TYPE_LABELS, type RecentCustomer } from '../../types';
import { formatRelativeTime } from '../../utils/formatters';

interface Props {
  items: RecentCustomer[];
}

export default function RecentCustomers({ items }: Props) {
  const navigate = useNavigate();

  return (
    <SectionRoot>
      <SectionHeader>
        <SectionTitle>최근 등록 고객사</SectionTitle>
        <SectionMore type="button" onClick={() => navigate('/customers')}>
          전체 보기
          <ArrowForwardRoundedIcon sx={{ fontSize: 14 }} />
        </SectionMore>
      </SectionHeader>
      {items.length === 0 ? (
        <EmptyState>등록된 고객사가 없습니다.</EmptyState>
      ) : (
        <ItemList>
          {items.map((c) => (
            <ItemRow key={c.id} onClick={() => navigate(`/customers/${c.id}`)}>
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
            </ItemRow>
          ))}
        </ItemList>
      )}
    </SectionRoot>
  );
}
