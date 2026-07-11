import { useNavigate } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import type { HeaderDetailField } from '@/shared/ui/GenericHeaderDetails';
import { formatDateTime } from '@/shared/ui/GenericTabbedTable';
import ExpenseStatusIndicator from '@/features/expense/components/ExpenseStatusIndicator';
import { useGetExpenseQuery } from '@/features/expense/api/expenseApi';
import { formatKrw } from '@/features/expense/utils/formatKrw';
import type { ExpenseDetail } from '@/features/expense/types';
import { useExpenseItemTab } from './useExpenseItemTab';

/**
 * 경비 청구 상세 page hook — fetching / 탭 오케스트레이션 / headerActions 묶음.
 * Hook 은 JSX 반환하지 않는다 (CLAUDE.md). 상세 헤더 필드는 detail 보장된 렌더 시점에
 * `expenseInfoFields(detail)` 를 page 가 호출하도록 builder 만 export.
 */
export function useExpenseDetailPage(expenseId: number) {
  const navigate = useNavigate();

  const detailQuery = useGetExpenseQuery(expenseId, { skip: !expenseId });

  const itemTab = useExpenseItemTab(expenseId, detailQuery.data?.items ?? []);

  const approvalDocumentId = detailQuery.data?.approvalDocumentId;

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
      onClick: () => navigate(MENU_PATH[MENU_CODE.EXPENSES]),
    },
    ...(approvalDocumentId
      ? [
          {
            design: 'secondary' as const,
            label: '결재 문서',
            icon: <ArticleOutlinedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.APPROVALS]}/${approvalDocumentId}`),
            menuCode: MENU_CODE.APPROVALS,
          },
        ]
      : []),
  ];

  return {
    queries: { detail: detailQuery },
    headerActions,
    tabsList: [itemTab.tab],
    tabs: {
      item: itemTab.modal,
    },
  };
}

export function expenseInfoFields(d: ExpenseDetail): HeaderDetailField[] {
  return [
    { label: '제목', value: d.title },
    { label: '상태', value: <ExpenseStatusIndicator status={d.status} /> },
    { label: '청구자', value: d.claimantName },
    { label: '총액', value: formatKrw(d.totalAmount) },
    { label: '청구일', value: formatDateTime(d.createdAt) },
    { label: '항목 수', value: `${d.items.length}건` },
  ];
}
