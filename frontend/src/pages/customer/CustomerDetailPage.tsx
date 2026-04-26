import { useParams } from 'react-router-dom';
import CustomerDetailForm from '@/features/customer/components/CustomerDetailForm/CustomerDetailForm';

export default function CustomerDetailPage() {
  const { id } = useParams<{ id: string }>();
  const customerId = id ? Number(id) : undefined;
  if (!customerId || Number.isNaN(customerId)) return null;
  return <CustomerDetailForm id={customerId} />;
}
