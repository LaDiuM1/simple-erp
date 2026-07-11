import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import PermissionGate from '@/shared/ui/layout/PermissionGate';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import FilterSelect from '@/shared/ui/atoms/FilterSelect';
import { MENU_CODE } from '@/shared/config/menuConfig';
import {
  SurfaceHeaderRow,
  SurfaceRoot,
  SurfaceTitle,
} from '@/features/attendance/components/attendanceSurface.styles';
import LeaveBalanceTable from '@/features/attendance/components/LeaveBalanceTable/LeaveBalanceTable';
import LeaveBalanceModals from '@/features/attendance/components/LeaveBalanceModals/LeaveBalanceModals';
import { useLeaveBalancePage } from '@/features/attendance/hooks/useLeaveBalancePage';
import { PageRoot } from './LeaveBalancePage.styles';

/** 전 직원 잔여 연차 관리 — 관리자 화면. 연도별 부여 / 사용 / 잔여 + 부여 조정 모달. */
export default function LeaveBalancePage() {
  const { queries, headerActions, yearFilter, onAdjust, modal } = useLeaveBalancePage();

  return (
    <PermissionGate
      menuCode={MENU_CODE.ATTENDANCE}
      action="write"
      fallback={<ErrorScreen message="잔여 연차 관리 권한이 없습니다." fullScreen={false} />}
    >
      <PageHeaderActions actions={headerActions} />
      <PageRoot>
        <SurfaceRoot>
          <SurfaceHeaderRow>
            <SurfaceTitle>직원별 잔여 연차</SurfaceTitle>
            <FilterSelect
              label="년도"
              allLabel="올해"
              value={yearFilter.year}
              onChange={yearFilter.onChange}
              options={yearFilter.options}
              minWidth={110}
            />
          </SurfaceHeaderRow>
          <QueryGate queries={queries}>
            {({ balances }) => <LeaveBalanceTable rows={balances} onAdjust={onAdjust} />}
          </QueryGate>
        </SurfaceRoot>
      </PageRoot>
      <LeaveBalanceModals modal={modal} />
    </PermissionGate>
  );
}
