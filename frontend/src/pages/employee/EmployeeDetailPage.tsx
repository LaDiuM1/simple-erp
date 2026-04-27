import { useParams } from 'react-router-dom';
import EmployeeDetailForm from '@/features/employee/components/EmployeeDetailForm/EmployeeDetailForm';

export default function EmployeeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const employeeId = id ? Number(id) : undefined;
  if (!employeeId || Number.isNaN(employeeId)) return null;
  return <EmployeeDetailForm id={employeeId} />;
}
