import { useFileDownload } from '@/shared/api/fileDownload';
import { formatFileSize } from '@/shared/utils/formatFileSize';
import {
  NowrapText,
  tabbedTab,
  type AnyTabbedTab,
  type TabbedTab,
  type TabbedTableColumn,
} from '@/shared/ui/GenericTabbedTable';
import type { ApprovalAttachment } from '@/features/approval/types';

/**
 * 첨부 탭 — read-only 목록 (파일명 / 크기). 행 클릭 시 결재 문서 첨부 엔드포인트로 다운로드
 * (문서 열람 권한이 있는 관련자만 접근 가능 — BE 가 documentId 기준으로 검증).
 */
export function useAttachmentTab(
  documentId: number,
  attachments: ApprovalAttachment[],
): { tab: AnyTabbedTab } {
  const download = useFileDownload();

  const columns: TabbedTableColumn<ApprovalAttachment>[] = [
    {
      key: 'name',
      header: '파일명',
      render: (f) => f.name,
    },
    {
      key: 'size',
      header: '크기',
      width: 96,
      render: (f) => <NowrapText>{formatFileSize(f.size)}</NowrapText>,
    },
  ];

  const tab: TabbedTab<ApprovalAttachment> = {
    key: 'attachments',
    label: '첨부',
    count: attachments.length,
    rows: attachments,
    rowKey: (f) => f.fileId,
    columns,
    emptyMessage: '첨부 파일이 없습니다.',
    onRowClick: (f) => {
      void download(`/api/v1/approvals/${documentId}/attachments/${f.fileId}`, f.name);
    },
  };

  return { tab: tabbedTab(tab) };
}
