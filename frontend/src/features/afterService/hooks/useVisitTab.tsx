import { useState } from 'react';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import Muted from '@/shared/ui/atoms/Muted';
import {
  NowrapText,
  TabPrimaryActionButton,
  TruncatedSpan,
  tabbedTab,
  type AnyTabbedTab,
  type TabbedTab,
  type TabbedTableColumn,
} from '@/shared/ui/GenericTabbedTable';
import type { ServiceVisit } from '@/features/afterService/types';

export interface VisitTabModalProps {
  afterServiceId: number;
  /** 수정 모달 대상 — canWrite 사용자의 행 클릭. */
  editing: ServiceVisit | null;
  creating: boolean;
  /** 읽기 전용 detail 모달 대상 — 쓰기 권한 없는 사용자의 행 클릭. */
  viewing: ServiceVisit | null;
  onCloseEdit: () => void;
  onCloseCreate: () => void;
  onCloseView: () => void;
}

/**
 * 방문 일지 탭 — 일 단위 기록 (서비스 리포트의 월별 시트 반복 행 대체).
 * 행 클릭: 쓰기 권한자는 수정 모달, 그 외는 읽기 전용 detail 모달.
 */
export function useVisitTab(
  afterServiceId: number,
  visits: ServiceVisit[],
): { tab: AnyTabbedTab; modal: VisitTabModalProps } {
  const { canWrite } = usePermission(MENU_CODE.AFTER_SERVICES);
  const [editing, setEditing] = useState<ServiceVisit | null>(null);
  const [creating, setCreating] = useState(false);
  const [viewing, setViewing] = useState<ServiceVisit | null>(null);

  const columns: TabbedTableColumn<ServiceVisit>[] = [
    {
      key: 'visitDate',
      header: '방문일',
      width: 108,
      render: (v) => <NowrapText>{v.visitDate}</NowrapText>,
    },
    {
      key: 'engineerName',
      header: '엔지니어',
      width: 110,
      render: (v) => v.engineerName ?? <Muted />,
    },
    {
      key: 'problem',
      header: '문제',
      render: (v) =>
        v.problem ? <TruncatedSpan maxWidth={260}>{v.problem}</TruncatedSpan> : <Muted />,
    },
    {
      key: 'resolution',
      header: '해결',
      render: (v) =>
        v.resolution ? <TruncatedSpan maxWidth={260}>{v.resolution}</TruncatedSpan> : <Muted />,
    },
  ];

  const tab: TabbedTab<ServiceVisit> = {
    key: 'visits',
    label: '방문 일지',
    count: visits.length,
    rows: visits,
    rowKey: (v) => v.id,
    columns,
    emptyMessage: '등록된 방문 일지가 없습니다.',
    rightSlot: canWrite ? (
      <TabPrimaryActionButton onClick={() => setCreating(true)}>일지 등록</TabPrimaryActionButton>
    ) : null,
    onRowClick: (v) => (canWrite ? setEditing(v) : setViewing(v)),
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
