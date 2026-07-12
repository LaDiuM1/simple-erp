import { useState } from 'react';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import Muted from '@/shared/ui/atoms/Muted';
import {
  InlineLinkButton,
  NowrapText,
  TabPrimaryActionButton,
  TruncatedSpan,
  formatDateTime,
  tabbedTab,
  type AnyTabbedTab,
  type TabbedTab,
  type TabbedTableColumn,
} from '@/shared/ui/GenericTabbedTable';
import type { ContractNote } from '@/features/contract/types';

export interface NoteTabModalProps {
  contractId: number;
  creating: boolean;
  /** 행 클릭 — 풀 컨텐츠 detail 모달 대상. */
  viewing: ContractNote | null;
  /** 삭제 확인 모달 대상. */
  deleting: ContractNote | null;
  onCloseCreate: () => void;
  onCloseView: () => void;
  onCloseDelete: () => void;
}

/**
 * 변경 이력 메모 탭 — 엑셀에서 셀 안에 줄바꿈으로 누적하던 변경 이력을 정규화된 행으로.
 * 수정 없음 (등록 / 삭제만) — 이력의 사후 조작을 막는 의도.
 */
export function useNoteTab(
  contractId: number,
  notes: ContractNote[],
): { tab: AnyTabbedTab; modal: NoteTabModalProps } {
  const { canWrite } = usePermission(MENU_CODE.CONTRACTS);
  const [creating, setCreating] = useState(false);
  const [viewing, setViewing] = useState<ContractNote | null>(null);
  const [deleting, setDeleting] = useState<ContractNote | null>(null);

  const columns: TabbedTableColumn<ContractNote>[] = [
    {
      key: 'createdAt',
      header: '일시',
      width: 140,
      render: (n) => <NowrapText>{formatDateTime(n.createdAt)}</NowrapText>,
    },
    {
      key: 'authorName',
      header: '작성자',
      width: 96,
      render: (n) => n.authorName ?? <Muted />,
    },
    {
      key: 'content',
      header: '내용',
      render: (n) => <TruncatedSpan maxWidth={420}>{n.content}</TruncatedSpan>,
    },
    {
      key: 'actions',
      header: '액션',
      align: 'right',
      width: 64,
      render: (n) =>
        canWrite ? (
          <InlineLinkButton
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              setDeleting(n);
            }}
          >
            삭제
          </InlineLinkButton>
        ) : (
          <Muted />
        ),
    },
  ];

  const tab: TabbedTab<ContractNote> = {
    key: 'notes',
    label: '변경 이력',
    count: notes.length,
    rows: notes,
    rowKey: (n) => n.id,
    columns,
    emptyMessage: '등록된 변경 이력 메모가 없습니다.',
    rightSlot: canWrite ? (
      <TabPrimaryActionButton onClick={() => setCreating(true)}>메모 등록</TabPrimaryActionButton>
    ) : null,
    onRowClick: (n) => setViewing(n),
  };

  return {
    tab: tabbedTab(tab),
    modal: {
      contractId,
      creating,
      viewing,
      deleting,
      onCloseCreate: () => setCreating(false),
      onCloseView: () => setViewing(null),
      onCloseDelete: () => setDeleting(null),
    },
  };
}
