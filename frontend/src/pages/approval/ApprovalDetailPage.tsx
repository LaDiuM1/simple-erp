import { useParams } from 'react-router-dom';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import GenericHeaderDetails from '@/shared/ui/GenericHeaderDetails';
import GenericTabbedTable from '@/shared/ui/GenericTabbedTable';
import {
  approvalInfoFields,
  useApprovalDetailPage,
} from '@/features/approval/hooks/useApprovalDetailPage';
import ApprovalDetailModals from '@/features/approval/components/ApprovalDetailModals/ApprovalDetailModals';
import { DetailRoot } from '@/features/approval/components/approvalDetail.styles';

export default function ApprovalDetailPage() {
  const { id } = useParams<{ id: string }>();
  const approvalId = Number(id);
  if (!approvalId) return null;

  const { queries, headerActions, tabsList, modal } = useApprovalDetailPage(approvalId);

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ detail }) => (
          <DetailRoot>
            <GenericHeaderDetails fields={approvalInfoFields(detail)} />
            <GenericTabbedTable tabs={tabsList} />
          </DetailRoot>
        )}
      </QueryGate>
      <ApprovalDetailModals modal={modal} />
    </>
  );
}
