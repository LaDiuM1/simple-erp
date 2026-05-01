import { useParams } from 'react-router-dom';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import CustomerDetailForm from '@/features/customer/components/CustomerDetailForm/CustomerDetailForm';
import { useCustomerDetailPage } from '@/features/customer/hooks/useCustomerDetailPage';

export default function CustomerDetailPage() {
  const { id } = useParams<{ id: string }>();
  const customerId = Number(id);
  if (!customerId) return null;

  const { queries } = useCustomerDetailPage(customerId);

  return (
    <QueryGate queries={queries}>
      {({ detail }) => <CustomerDetailForm id={customerId} detail={detail} />}
    </QueryGate>
  );
}
