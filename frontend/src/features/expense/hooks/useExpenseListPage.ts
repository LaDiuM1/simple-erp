import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { FilterConfig, ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import { useGetExpensesQuery } from '@/features/expense/api/expenseApi';
import {
  expenseListFilters,
  expenseScopeFilter,
} from '@/features/expense/config/expenseListConfig';
import type { ExpenseListFilters, ExpenseSummary } from '@/features/expense/types';

/**
 * 경비 청구 목록 page hook — api + searchFilter + headerActions 묶음.
 * 기본은 본인 청구만 (MINE) — EXPENSES 쓰기 권한자 (정산 담당) 에게만 조회 범위 필터를
 * 동적으로 prepend 해 전체 (ALL) 조회를 연다. 생성 = 즉시 상신이라 수정/삭제 액션 없음 — 행 클릭 = 상세 진입.
 */
export function useExpenseListPage(): {
  api: ListApiConfig<ExpenseSummary, ExpenseListFilters>;
  searchFilter: FilterConfig[];
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.EXPENSES);

  const api: ListApiConfig<ExpenseSummary, ExpenseListFilters> = {
    menuCode: MENU_CODE.EXPENSES,
    useList: useGetExpensesQuery,
    rowKey: (m) => m.id,
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.EXPENSES]}/${m.id}`),
    emptyMessage: '등록된 경비 청구가 없습니다.',
  };

  const searchFilter: FilterConfig[] = canWrite
    ? [expenseScopeFilter, ...expenseListFilters]
    : expenseListFilters;

  const headerActions: PageHeaderAction[] = [
    {
      design: 'create',
      label: '경비 등록',
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.EXPENSES]}/new`),
      menuCode: MENU_CODE.EXPENSES,
    },
  ];

  return { api, searchFilter, headerActions };
}
