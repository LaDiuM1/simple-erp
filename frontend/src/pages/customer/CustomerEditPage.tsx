import { useParams } from 'react-router-dom';
import CustomerEditForm from '@/features/customer/components/CustomerEditForm/CustomerEditForm';

export default function CustomerEditPage() {
  const { id } = useParams<{ id: string }>();
  const customerId = id ? Number(id) : undefined;
  if (!customerId || Number.isNaN(customerId)) return null;
  return <CustomerEditForm id={customerId} />;
}
