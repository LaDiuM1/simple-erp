import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useGetApprovalsQuery } from '@/features/approval/api/approvalApi';
import {
  type ApprovalListFilters,
  type ApprovalSummary,
} from '@/features/approval/types';

/**
 * 전자결재 목록 page hook — api + headerActions 묶음.
 * 결재 문서는 삭제 / 엑셀 없음 — 행 클릭 → 상세, 기안 작성 → /approvals/new.
 */
export function useApprovalListPage(): {
  api: ListApiConfig<ApprovalSummary, ApprovalListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<ApprovalSummary, ApprovalListFilters> = {
    menuCode: MENU_CODE.APPROVALS,
    useList: useGetApprovalsQuery,
    rowKey: (a) => a.id,
    onRowClick: (a) => navigate(`${MENU_PATH[MENU_CODE.APPROVALS]}/${a.id}`),
    emptyMessage: '조건에 맞는 결재 문서가 없습니다.',
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'create',
      label: '기안 작성',
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.APPROVALS]}/new`),
      menuCode: MENU_CODE.APPROVALS,
    },
  ];

  return { api, headerActions };
}
