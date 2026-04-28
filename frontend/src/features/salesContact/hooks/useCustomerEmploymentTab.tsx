import { useNavigate } from 'react-router-dom';
import {
  NowrapText,
  StatusText,
  tabbedTab,
  type TabbedTab,
  type TabbedTableColumn,
  type TabHookResult,
} from '@/shared/ui/GenericTabbedTable';
import Muted from '@/shared/ui/atoms/Muted';
import { MENU_PATH, MENU_CODE } from '@/shared/config/menuConfig';
import {
  DEPARTURE_TYPE_LABELS,
  type SalesContactEmployment,
} from '@/features/salesContact/types';

/**
 * 고객사 상세의 "소속 영업 명부" 탭 — read-only.
 * 행 클릭 시 해당 명부 상세 (`/sales-contacts/:contactId`) 로 navigate.
 * 비활성 행은 opacity dim, 상태 텍스트는 활성=primary / 비활성=disabled 색.
 *
 * employments 데이터는 outer 가 `useGetSalesContactEmploymentsByCustomerIdQuery` 로 fetch 후 전달 (CLAUDE.md hook 정책).
 */
export function useCustomerEmploymentTab(
  employments: SalesContactEmployment[],
): TabHookResult {
  const navigate = useNavigate();

  const columns: TabbedTableColumn<SalesContactEmployment>[] = [
    {
      key: 'status',
      header: '상태',
      width: 88,
      render: (e) =>
        e.active ? (
          <StatusText tone="primary">재직 중</StatusText>
        ) : (
          <StatusText tone="disabled">
            {e.departureType ? DEPARTURE_TYPE_LABELS[e.departureType] : '종료'}
          </StatusText>
        ),
    },
    {
      key: 'contact',
      header: '명부',
      render: (e) => e.contactName ?? <Muted />,
    },
    {
      key: 'position',
      header: '직책',
      width: 132,
      render: (e) => e.position ?? <Muted />,
    },
    {
      key: 'department',
      header: '부서',
      width: 144,
      render: (e) => e.department ?? <Muted />,
    },
    {
      key: 'startDate',
      header: '시작일',
      width: 108,
      render: (e) => <NowrapText>{e.startDate}</NowrapText>,
    },
    {
      key: 'endDate',
      header: '종료일',
      width: 108,
      render: (e) => (e.endDate ? <NowrapText>{e.endDate}</NowrapText> : <Muted />),
    },
    {
      key: 'departureNote',
      header: '퇴직 메모',
      render: (e) => e.departureNote ?? <Muted />,
    },
  ];

  const tab: TabbedTab<SalesContactEmployment> = {
    key: 'customerEmployments',
    label: '소속 영업 명부',
    count: employments.length,
    rows: employments,
    rowKey: (e) => e.id,
    rowSx: (e) => ({ opacity: e.active ? 1 : 0.7 }),
    columns,
    emptyMessage: '이 고객사 소속으로 등록된 영업 명부가 없습니다.',
    onRowClick: (e) => navigate(`${MENU_PATH[MENU_CODE.SALES_CONTACTS]}/${e.contactId}`),
  };

  return { tab: tabbedTab(tab), modals: null };
}
