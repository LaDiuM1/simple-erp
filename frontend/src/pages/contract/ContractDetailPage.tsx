import { useParams } from 'react-router-dom';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import GenericHeaderDetails from '@/shared/ui/GenericHeaderDetails';
import GenericTabbedTable from '@/shared/ui/GenericTabbedTable';
import {
  contractInfoFields,
  useContractDetailPage,
} from '@/features/contract/hooks/useContractDetailPage';
import PaymentTabModals from '@/features/contract/components/PaymentTabModals/PaymentTabModals';
import NoteTabModals from '@/features/contract/components/NoteTabModals/NoteTabModals';
import { DetailRoot } from '@/features/contract/components/contractDetail.styles';

export default function ContractDetailPage() {
  const { id } = useParams<{ id: string }>();
  const contractId = Number(id);
  return contractId ? <ContractDetailContent contractId={contractId} /> : null;
}

function ContractDetailContent({ contractId }: { contractId: number }) {
  const { queries, headerActions, tabsList, tabs } = useContractDetailPage(contractId);

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ detail }) => (
          <DetailRoot>
            <GenericHeaderDetails fields={contractInfoFields(detail)} sticky={false} />
            <GenericTabbedTable tabs={tabsList} />
          </DetailRoot>
        )}
      </QueryGate>
      <PaymentTabModals modal={tabs.payment} />
      <NoteTabModals modal={tabs.note} />
    </>
  );
}
