import {
  tabbedTab,
  type TabbedTab,
  type TabHookResult,
} from '@/shared/ui/GenericTabbedTable';
import {
  ACQUISITION_SOURCE_TYPE_LABELS,
  type AcquisitionSourceInfo,
} from '@/features/acquisitionSource/types';

/**
 * 컨택 경로 탭 — 단순 read-only 목록 (이름 / 분류 2-column).
 * 정렬은 BE 가 type ASC → name ASC 로 미리 처리.
 */
export function useSourceTab(sources: AcquisitionSourceInfo[]): TabHookResult {
  const tab: TabbedTab<AcquisitionSourceInfo> = {
    key: 'sources',
    label: '컨택 경로',
    count: sources.length,
    rows: sources,
    rowKey: (r) => r.id,
    emptyMessage: '등록된 컨택 경로가 없습니다.',
    columns: [
      {
        key: 'type',
        header: '분류',
        width: 160,
        render: (r) => ACQUISITION_SOURCE_TYPE_LABELS[r.type],
      },
      {
        key: 'name',
        header: '이름',
        render: (r) => r.name,
      },
    ],
  };
  return { tab: tabbedTab(tab), modals: null };
}
