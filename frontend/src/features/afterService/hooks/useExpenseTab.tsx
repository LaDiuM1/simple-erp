import { useState } from 'react';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import Muted from '@/shared/ui/atoms/Muted';
import { formatKrw } from '@/shared/utils/formatKrw';
import {
  NowrapText,
  StatusText,
  TabPrimaryActionButton,
  TruncatedSpan,
  tabbedTab,
  type AnyTabbedTab,
  type TabbedTab,
  type TabbedTableColumn,
} from '@/shared/ui/GenericTabbedTable';
import {
  EXPENSE_PAYER_TYPE_LABELS,
  SERVICE_EXPENSE_CATEGORY_LABELS,
  type ServiceExpense,
} from '@/features/afterService/types';

export interface ExpenseTabModalProps {
  afterServiceId: number;
  editing: ServiceExpense | null;
  creating: boolean;
  viewing: ServiceExpense | null;
  onCloseEdit: () => void;
  onCloseCreate: () => void;
  onCloseView: () => void;
}

/**
 * 경비 탭 — AS 건의 원가 기록. 숙소 결제 내역은 "숙박비 + 회사 직접결제" 행으로 이 탭에 흡수.
 * 행 클릭: 쓰기 권한자는 수정 모달, 그 외는 읽기 전용 detail 모달.
 */
export function useExpenseTab(
  afterServiceId: number,
  expenses: ServiceExpense[],
): { tab: AnyTabbedTab; modal: ExpenseTabModalProps } {
  const { canWrite } = usePermission(MENU_CODE.AFTER_SERVICES);
  const [editing, setEditing] = useState<ServiceExpense | null>(null);
  const [creating, setCreating] = useState(false);
  const [viewing, setViewing] = useState<ServiceExpense | null>(null);

  const columns: TabbedTableColumn<ServiceExpense>[] = [
    {
      key: 'category',
      header: '분류',
      width: 90,
      render: (e) => SERVICE_EXPENSE_CATEGORY_LABELS[e.category],
    },
    {
      key: 'amount',
      header: '금액',
      align: 'right',
      width: 120,
      render: (e) => formatKrw(e.amount),
    },
    {
      key: 'payerType',
      header: '결제 주체',
      width: 120,
      render: (e) => (
        <StatusText tone={e.payerType === 'COMPANY' ? 'primary' : 'secondary'}>
          {EXPENSE_PAYER_TYPE_LABELS[e.payerType]}
        </StatusText>
      ),
    },
    {
      key: 'paidDate',
      header: '결제일',
      width: 108,
      render: (e) => (e.paidDate ? <NowrapText>{e.paidDate}</NowrapText> : <Muted />),
    },
    {
      key: 'engineerName',
      header: '엔지니어',
      width: 110,
      render: (e) => e.engineerName ?? <Muted />,
    },
    {
      key: 'note',
      header: '메모',
      render: (e) => (e.note ? <TruncatedSpan maxWidth={200}>{e.note}</TruncatedSpan> : <Muted />),
    },
  ];

  const tab: TabbedTab<ServiceExpense> = {
    key: 'expenses',
    label: '경비',
    count: expenses.length,
    rows: expenses,
    rowKey: (e) => e.id,
    columns,
    emptyMessage: '등록된 경비가 없습니다.',
    rightSlot: canWrite ? (
      <TabPrimaryActionButton onClick={() => setCreating(true)}>경비 등록</TabPrimaryActionButton>
    ) : null,
    onRowClick: (e) => (canWrite ? setEditing(e) : setViewing(e)),
  };

  return {
    tab: tabbedTab(tab),
    modal: {
      afterServiceId,
      editing,
      creating,
      viewing,
      onCloseEdit: () => setEditing(null),
      onCloseCreate: () => setCreating(false),
      onCloseView: () => setViewing(null),
    },
  };
}
