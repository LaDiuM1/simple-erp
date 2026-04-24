import StatusDot from '@/shared/ui/atoms/StatusDot';
import { MEMBER_STATUS_LABELS, type MemberStatus } from '@/features/member/types';

const MEMBER_STATUS_COLOR: Record<MemberStatus, string> = {
  ACTIVE: '#10B981',
  LEAVE: '#F59E0B',
  RESIGNED: '#94A3B8',
};

/**
 * MemberStatus 전용 래퍼.
 * 상태값을 받아 색/라벨 매핑과 함께 공용 StatusDot 에 전달한다.
 */
export default function MemberStatusIndicator({ status }: { status: MemberStatus }) {
  return <StatusDot color={MEMBER_STATUS_COLOR[status]} label={MEMBER_STATUS_LABELS[status]} />;
}
