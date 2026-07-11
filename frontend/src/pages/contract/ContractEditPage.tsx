import { useParams } from 'react-router-dom';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import ContractEditForm from '@/features/contract/components/ContractEditForm/ContractEditForm';
import { useContractEditPage } from '@/features/contract/hooks/useContractEditPage';

export default function ContractEditPage() {
  const { id } = useParams<{ id: string }>();
  const contractId = Number(id);
  if (!contractId) return null;

  const { queries } = useContractEditPage(contractId);

  return (
    <QueryGate queries={queries}>
      {({ detail }) => <ContractEditForm id={contractId} detail={detail} />}
    </QueryGate>
  );
}
