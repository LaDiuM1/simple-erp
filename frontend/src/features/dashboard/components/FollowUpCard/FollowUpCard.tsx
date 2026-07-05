import type { FollowUpCustomer } from '../../types';
import { daysSince, formatShortDate } from '../../utils/formatters';
import {
  CardHead,
  CardRoot,
  CardTitle,
  CountBadge,
  ElapsedText,
  EmptyText,
  FollowBody,
  FollowMeta,
  FollowName,
  FollowRow,
  MoreLink,
} from './FollowUpCard.styles';

interface Props {
  items: FollowUpCustomer[];
  onItemClick: (customerId: number) => void;
  onMore: () => void;
}

/**
 * 팔로업 필요 고객 — 이 대시보드의 "오늘 뭘 해야 하지?" 에 대한 직접적인 답.
 * 마지막 접촉이 오래된 순 (활동 없음 최우선), 행 클릭 시 해당 고객사 영업 상세로 이동.
 */
export default function FollowUpCard({ items, onItemClick, onMore }: Props) {
  return (
    <CardRoot>
      <CardHead>
        <CardTitle>
          팔로업 필요
          {items.length > 0 && <CountBadge>{items.length}</CountBadge>}
        </CardTitle>
        <MoreLink type="button" onClick={onMore}>
          전체 보기
        </MoreLink>
      </CardHead>
      {items.length === 0 ? (
        <EmptyText>팔로업이 필요한 고객이 없어요 — 잘 관리되고 있습니다.</EmptyText>
      ) : (
        items.map((item) => (
          <FollowRow
            key={item.customerId}
            type="button"
            onClick={() => onItemClick(item.customerId)}
          >
            <FollowBody>
              <FollowName>{item.customerName ?? item.customerCode ?? '-'}</FollowName>
              <FollowMeta>
                {item.primaryAssigneeName ? `담당 ${item.primaryAssigneeName}` : '주담당 없음'}
                {item.lastActivityDate &&
                  ` · 마지막 접촉 ${formatShortDate(item.lastActivityDate)}`}
              </FollowMeta>
            </FollowBody>
            <ElapsedText>
              {item.lastActivityDate ? `${daysSince(item.lastActivityDate)}일 경과` : '기록 없음'}
            </ElapsedText>
          </FollowRow>
        ))
      )}
    </CardRoot>
  );
}
