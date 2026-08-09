import type { ReactNode } from 'react';
import ArrowForwardRoundedIcon from '@mui/icons-material/ArrowForwardRounded';
import DirectionsWalkRoundedIcon from '@mui/icons-material/DirectionsWalkRounded';
import LocalPhoneRoundedIcon from '@mui/icons-material/LocalPhoneRounded';
import GroupsRoundedIcon from '@mui/icons-material/GroupsRounded';
import EmailRoundedIcon from '@mui/icons-material/EmailRounded';
import MoreHorizRoundedIcon from '@mui/icons-material/MoreHorizRounded';
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
  listPath?: string;
  detailPath?: (customerId: number) => string;
}

const TYPE_ICON: Record<SalesActivityType, ReactNode> = {
  VISIT: <DirectionsWalkRoundedIcon sx={{ fontSize: 18 }} />,
  CALL: <LocalPhoneRoundedIcon sx={{ fontSize: 18 }} />,
  MEETING: <GroupsRoundedIcon sx={{ fontSize: 18 }} />,
  EMAIL: <EmailRoundedIcon sx={{ fontSize: 18 }} />,
  OTHER: <MoreHorizRoundedIcon sx={{ fontSize: 18 }} />,
};

export default function RecentActivities({ items, listPath, detailPath }: Props) {
  return (
    <SectionRoot>
      <SectionHeader>
        <SectionHeading>
          <SectionTitle>
            최근 영업 활동
            <SectionCount>{items.length}</SectionCount>
          </SectionTitle>
          <SectionDescription>최근 고객 접점과 담당자를 이어서 확인해요.</SectionDescription>
        </SectionHeading>
        {listPath && (
          <SectionMore to={listPath}>
            전체 보기
            <ArrowForwardRoundedIcon sx={{ fontSize: 14 }} />
          </SectionMore>
        )}
      </SectionHeader>
      {items.length === 0 ? (
        <EmptyState>등록된 영업 활동이 없습니다.</EmptyState>
      ) : (
        <ItemList>
          {items.map((a) => {
            const content = (
              <>
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
              </>
            );

            return (
              <ItemRow key={a.id}>
                {detailPath ? (
                  <ItemAction to={detailPath(a.customerId)}>
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
