import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import LeaveBalanceCard from '@/features/attendance/components/LeaveBalanceCard/LeaveBalanceCard';
import LeaveListTable from '@/features/attendance/components/LeaveListTable/LeaveListTable';
import { useLeaveMePage } from '@/features/attendance/hooks/useLeaveMePage';
import { PageRoot } from './LeaveMePage.styles';

/** 휴가 관리 — 잔여 연차 카드 + 내 신청 목록. */
export default function LeaveMePage() {
  const { queries, headerActions, onLeaveRowClick } = useLeaveMePage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ leaves, balance }) => (
          <PageRoot>
            <LeaveBalanceCard balance={balance} />
            <LeaveListTable rows={leaves} onRowClick={onLeaveRowClick} />
          </PageRoot>
        )}
      </QueryGate>
    </>
  );
}
