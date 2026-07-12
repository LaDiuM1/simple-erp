import { useState } from 'react';
import {
  InlineLinkButton,
  NowrapText,
  TruncatedSpan,
  tabbedTab,
  type AnyTabbedTab,
  type TabbedTab,
  type TabbedTableColumn,
} from '@/shared/ui/GenericTabbedTable';
import { type DetailModalField } from '@/shared/ui/GenericDetailModal';
import Muted from '@/shared/ui/atoms/Muted';
import { useFileDownload } from '@/shared/api/fileDownload';
import { formatKrw } from '@/shared/utils/formatKrw';
import { EXPENSE_CATEGORY_LABELS, type ExpenseItem } from '@/features/expense/types';
import type { ExpenseItemTabModalProps } from '@/features/expense/components/ExpenseItemTabModals/ExpenseItemTabModals';

/**
 * 경비 항목 탭 — read-only (생성 = 즉시 상신이라 상세에서 항목 편집 없음).
 * 내용 셀은 ellipsis 로 잘리고 행 클릭 시 GenericDetailModal 로 풀 컨텐츠 노출.
 * 영수증 다운로드는 행 클릭과 분리 (stopPropagation) — 청구 단위 엔드포인트로 다운로드
 * (본인 / 결재 관련자 / 쓰기 권한자만 접근 가능 — BE 가 claimId 기준으로 검증).
 *
 * items 데이터는 outer 가 fetch 후 전달. Hook 은 JSX 반환하지 않음 (CLAUDE.md).
 */
export function useExpenseItemTab(
  claimId: number,
  items: ExpenseItem[],
): { tab: AnyTabbedTab; modal: ExpenseItemTabModalProps } {
  const download = useFileDownload();
  const [detailTarget, setDetailTarget] = useState<ExpenseItem | null>(null);

  const renderReceipt = (item: ExpenseItem) => {
    const fileId = item.receiptFileId;
    if (fileId == null) return <Muted />;
    const fileName = item.receiptFileName ?? '영수증';
    return (
      <InlineLinkButton
        type="button"
        onClick={(ev) => {
          ev.stopPropagation();
          void download(`/api/v1/expenses/${claimId}/receipts/${fileId}`, fileName);
        }}
      >
        {fileName}
      </InlineLinkButton>
    );
  };

  const columns: TabbedTableColumn<ExpenseItem>[] = [
    {
      key: 'expenseDate',
      header: '일자',
      width: 108,
      render: (item) => <NowrapText>{item.expenseDate}</NowrapText>,
    },
    {
      key: 'category',
      header: '분류',
      width: 120,
      render: (item) => EXPENSE_CATEGORY_LABELS[item.category],
    },
    {
      key: 'amount',
      header: '금액',
      align: 'right',
      width: 140,
      render: (item) => <NowrapText>{formatKrw(item.amount)}</NowrapText>,
    },
    {
      key: 'description',
      header: '내용',
      render: (item) =>
        item.description ? (
          <TruncatedSpan maxWidth={320}>{item.description}</TruncatedSpan>
        ) : (
          <Muted />
        ),
    },
    {
      key: 'receipt',
      header: '영수증',
      width: 200,
      render: renderReceipt,
    },
  ];

  const tab: TabbedTab<ExpenseItem> = {
    key: 'items',
    label: '경비 항목',
    count: items.length,
    rows: items,
    rowKey: (item) => item.id,
    columns,
    emptyMessage: '등록된 경비 항목이 없습니다.',
    onRowClick: (item) => setDetailTarget(item),
  };

  const detailFields: DetailModalField[] = detailTarget
    ? [
        { label: '일자', value: detailTarget.expenseDate },
        { label: '분류', value: EXPENSE_CATEGORY_LABELS[detailTarget.category] },
        { label: '금액', value: formatKrw(detailTarget.amount) },
        { label: '내용', value: detailTarget.description },
        { label: '영수증', value: renderReceipt(detailTarget) },
      ]
    : [];

  const modal: ExpenseItemTabModalProps = {
    detailTarget,
    detailFields,
    onCloseDetail: () => setDetailTarget(null),
  };

  return { tab: tabbedTab(tab), modal };
}
