import { useState } from 'react';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import Muted from '@/shared/ui/atoms/Muted';
import { formatKrw } from '@/shared/utils/formatKrw';
import {
  NowrapText,
  TabPrimaryActionButton,
  TruncatedSpan,
  tabbedTab,
  type AnyTabbedTab,
  type TabbedTab,
  type TabbedTableColumn,
} from '@/shared/ui/GenericTabbedTable';
import type { ContractPayment } from '@/features/contract/types';

export interface PaymentTabModalProps {
  contractId: number;
  /** 수정 모달 대상 — canWrite 사용자의 행 클릭. */
  editing: ContractPayment | null;
  /** 등록 모달 오픈 여부. */
  creating: boolean;
  /** 읽기 전용 detail 모달 대상 — 쓰기 권한 없는 사용자의 행 클릭. */
  viewing: ContractPayment | null;
  onCloseEdit: () => void;
  onCloseCreate: () => void;
  onCloseView: () => void;
}

/**
 * 대금 스케줄 탭 — 회차별 예정 / 입금 / 세금계산서 매트릭스.
 * 행 클릭: 쓰기 권한자는 수정 모달, 그 외는 읽기 전용 detail 모달.
 */
export function usePaymentTab(
  contractId: number,
  payments: ContractPayment[],
): { tab: AnyTabbedTab; modal: PaymentTabModalProps } {
  const { canWrite } = usePermission(MENU_CODE.CONTRACTS);
  const [editing, setEditing] = useState<ContractPayment | null>(null);
  const [creating, setCreating] = useState(false);
  const [viewing, setViewing] = useState<ContractPayment | null>(null);

  const columns: TabbedTableColumn<ContractPayment>[] = [
    { key: 'label', header: '회차', width: 96, render: (p) => p.label },
    {
      key: 'plannedDate',
      header: '예정일',
      width: 108,
      render: (p) => (p.plannedDate ? <NowrapText>{p.plannedDate}</NowrapText> : <Muted />),
    },
    {
      key: 'plannedAmount',
      header: '예정액',
      align: 'right',
      width: 120,
      render: (p) => (p.plannedAmount == null ? <Muted /> : formatKrw(p.plannedAmount)),
    },
    {
      key: 'paidDate',
      header: '입금일',
      width: 108,
      render: (p) => (p.paidDate ? <NowrapText>{p.paidDate}</NowrapText> : <Muted />),
    },
    {
      key: 'paidAmount',
      header: '입금액',
      align: 'right',
      width: 120,
      render: (p) => (p.paidAmount == null ? <Muted /> : formatKrw(p.paidAmount)),
    },
    {
      key: 'invoiceDate',
      header: '계산서 발행일',
      width: 118,
      render: (p) => (p.invoiceDate ? <NowrapText>{p.invoiceDate}</NowrapText> : <Muted />),
    },
    {
      key: 'invoiceAmount',
      header: '계산서 금액',
      align: 'right',
      width: 120,
      render: (p) => (p.invoiceAmount == null ? <Muted /> : formatKrw(p.invoiceAmount)),
    },
    {
      key: 'note',
      header: '메모',
      render: (p) =>
        p.note ? <TruncatedSpan maxWidth={160}>{p.note}</TruncatedSpan> : <Muted />,
    },
  ];

  const tab: TabbedTab<ContractPayment> = {
    key: 'payments',
    label: '대금 스케줄',
    count: payments.length,
    rows: payments,
    rowKey: (p) => p.id,
    columns,
    emptyMessage: '등록된 대금 회차가 없습니다.',
    rightSlot: canWrite ? (
      <TabPrimaryActionButton onClick={() => setCreating(true)}>회차 등록</TabPrimaryActionButton>
    ) : null,
    onRowClick: (p) => (canWrite ? setEditing(p) : setViewing(p)),
  };

  return {
    tab: tabbedTab(tab),
    modal: {
      contractId,
      editing,
      creating,
      viewing,
      onCloseEdit: () => setEditing(null),
      onCloseCreate: () => setCreating(false),
      onCloseView: () => setViewing(null),
    },
  };
}
