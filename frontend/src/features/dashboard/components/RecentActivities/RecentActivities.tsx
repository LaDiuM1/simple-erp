import type { ReactNode } from 'react';
import { useNavigate } from 'react-router-dom';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import DirectionsWalkRoundedIcon from '@mui/icons-material/DirectionsWalkRounded';
import LocalPhoneRoundedIcon from '@mui/icons-material/LocalPhoneRounded';
import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import EmailRoundedIcon from '@mui/icons-material/EmailRounded';
import MoreHorizRoundedIcon from '@mui/icons-material/MoreHorizRounded';
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
import {
  ActivityIcon,
  ActivityTypeLabel,
  MetaSeparator,
} from './RecentActivities.styles';
import {
  SALES_ACTIVITY_TYPE_LABELS,
  type RecentSalesActivity,
  type SalesActivityType,
} from '../../types';
import { formatRelativeTime } from '../../utils/formatters';

interface Props {
  items: RecentSalesActivity[];
}

const TYPE_ICON: Record<SalesActivityType, ReactNode> = {
  VISIT: <DirectionsWalkRoundedIcon sx={{ fontSize: 18 }} />,
  CALL: <LocalPhoneRoundedIcon sx={{ fontSize: 18 }} />,
  MEETING: <GroupsRoundedIcon sx={{ fontSize: 18 }} />,
  EMAIL: <EmailRoundedIcon sx={{ fontSize: 18 }} />,
  OTHER: <MoreHorizRoundedIcon sx={{ fontSize: 18 }} />,
};

export default function RecentActivities({ items }: Props) {
  const navigate = useNavigate();

  return (
    <SectionRoot>
      <SectionHeader>
        <SectionTitle>최근 영업 활동</SectionTitle>
        <SectionMore type="button" onClick={() => navigate('/sales-customers')}>
          전체 보기
          <ArrowForwardRoundedIcon sx={{ fontSize: 14 }} />
        </SectionMore>
      </SectionHeader>
      {items.length === 0 ? (
        <EmptyState>등록된 영업 활동이 없습니다.</EmptyState>
      ) : (
        <ItemList>
          {items.map((a) => (
            <ItemRow
              key={a.id}
              onClick={() => navigate(`/sales-customers/${a.customerId}`)}
            >
              <ActivityIcon>{TYPE_ICON[a.type]}</ActivityIcon>
              <ItemMain>
                <ItemTopLine>
                  <ItemTitle>{a.subject}</ItemTitle>
                </ItemTopLine>
                <ItemMeta>
                  <ActivityTypeLabel>{SALES_ACTIVITY_TYPE_LABELS[a.type]}</ActivityTypeLabel>
                  {a.customerName && (
                    <>
                      <MetaSeparator />
                      <span>{a.customerName}</span>
                    </>
                  )}
                  {a.ourEmployeeName && (
                    <>
                      <MetaSeparator />
                      <span>{a.ourEmployeeName}</span>
                    </>
                  )}
                </ItemMeta>
              </ItemMain>
              <ItemTime>{formatRelativeTime(a.activityDate)}</ItemTime>
            </ItemRow>
          ))}
        </ItemList>
      )}
    </SectionRoot>
  );
}
