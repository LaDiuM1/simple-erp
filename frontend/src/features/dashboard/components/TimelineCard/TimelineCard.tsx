import { SALES_ACTIVITY_TYPE_LABELS, type RecentSalesActivity } from '../../types';
import { formatRelativeTime } from '../../utils/formatters';
import {
  CardHead,
  CardRoot,
  CardTitle,
  EmptyText,
  MoreLink,
  RowBody,
  RowMeta,
  RowTime,
  RowTitle,
  TimelineDot,
  TimelineLine,
  TimelineRail,
  TimelineRow,
} from './TimelineCard.styles';

interface Props {
  items: RecentSalesActivity[];
  onItemClick: (customerId: number) => void;
  onMore: () => void;
}

/**
 * 최근 영업 활동 타임라인 — 팀이 지금 뭘 하고 있는지의 맥박.
 * 최신 항목만 코발트 dot 으로 강조, 아이콘 대신 유형 텍스트 라벨 (시각 차등은 텍스트로).
 */
export default function TimelineCard({ items, onItemClick, onMore }: Props) {
  return (
    <CardRoot>
      <CardHead>
        <CardTitle>최근 활동</CardTitle>
        <MoreLink type="button" onClick={onMore}>
          전체 보기
        </MoreLink>
      </CardHead>
      {items.length === 0 ? (
        <EmptyText>등록된 영업 활동이 없습니다.</EmptyText>
      ) : (
        items.map((activity, index) => (
          <TimelineRow
            key={activity.id}
            type="button"
            onClick={() => onItemClick(activity.customerId)}
          >
            <TimelineRail>
              <TimelineDot latest={index === 0} />
              {index < items.length - 1 && <TimelineLine />}
            </TimelineRail>
            <RowBody>
              <RowTitle>
                <i>{SALES_ACTIVITY_TYPE_LABELS[activity.type]}</i>
                {activity.customerName ?? activity.customerCode ?? '-'}
              </RowTitle>
              <RowMeta>
                {activity.subject}
                {activity.ourEmployeeName && ` · ${activity.ourEmployeeName}`}
              </RowMeta>
            </RowBody>
            <RowTime>{formatRelativeTime(activity.activityDate)}</RowTime>
          </TimelineRow>
        ))
      )}
    </CardRoot>
  );
}
