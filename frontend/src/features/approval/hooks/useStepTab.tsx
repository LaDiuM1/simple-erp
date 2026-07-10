import {
  NowrapText,
  StatusText,
  formatDateTime,
  tabbedTab,
  type AnyTabbedTab,
  type StatusTone,
  type TabbedTab,
  type TabbedTableColumn,
} from '@/shared/ui/GenericTabbedTable';
import Muted from '@/shared/ui/atoms/Muted';
import { STEP_STATUS_LABELS, type ApprovalStep } from '@/features/approval/types';

/**
 * 결재선 탭 — read-only (순번 / 결재자 / 상태 / 의견 / 처리일시). 모달 없음.
 * activeStepOrder 는 진행 중 문서의 현재 차례 (그 외 0) — 해당 대기 스텝만 primary 로 강조.
 */
export function useStepTab(
  steps: ApprovalStep[],
  activeStepOrder: number,
): { tab: AnyTabbedTab } {
  const columns: TabbedTableColumn<ApprovalStep>[] = [
    {
      key: 'stepOrder',
      header: '순번',
      width: 72,
      render: (s) => `${s.stepOrder}차`,
    },
    {
      key: 'approverName',
      header: '결재자',
      width: 160,
      render: (s) => s.approverName,
    },
    {
      key: 'status',
      header: '상태',
      width: 96,
      render: (s) => (
        <StatusText tone={stepTone(s, activeStepOrder)}>
          {STEP_STATUS_LABELS[s.status]}
        </StatusText>
      ),
    },
    {
      key: 'comment',
      header: '의견',
      render: (s) => s.comment ?? <Muted />,
    },
    {
      key: 'decidedAt',
      header: '처리일시',
      width: 140,
      render: (s) => (s.decidedAt ? <NowrapText>{formatDateTime(s.decidedAt)}</NowrapText> : <Muted />),
    },
  ];

  const tab: TabbedTab<ApprovalStep> = {
    key: 'steps',
    label: '결재선',
    count: steps.length,
    rows: steps,
    rowKey: (s) => s.stepOrder,
    columns,
    emptyMessage: '결재선이 없습니다.',
  };

  return { tab: tabbedTab(tab) };
}

function stepTone(step: ApprovalStep, activeStepOrder: number): StatusTone {
  // 승인은 헤더 ApprovalStatusIndicator 와 동일 색 (statusActive) — 같은 상태가 같은 색으로 보이도록.
  if (step.status === 'APPROVED') return 'active';
  if (step.status === 'REJECTED') return 'warning';
  return step.stepOrder === activeStepOrder ? 'primary' : 'disabled';
}
