import { useParams } from 'react-router-dom';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import CustomerEditForm from '@/features/customer/components/CustomerEditForm/CustomerEditForm';
import { useCustomerEditPage } from '@/features/customer/hooks/useCustomerEditPage';

export default function CustomerEditPage() {
  const { id } = useParams<{ id: string }>();
  const customerId = Number(id);
  if (!customerId) return null;

  const { queries } = useCustomerEditPage(customerId);

  return (
    <QueryGate queries={queries}>
      {({ detail }) => <CustomerEditForm id={customerId} detail={detail} />}
    </QueryGate>
  );
}
