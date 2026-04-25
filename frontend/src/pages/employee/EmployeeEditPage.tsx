import { useParams } from 'react-router-dom';
import EmployeeEditForm from '@/features/employee/components/EmployeeEditForm/EmployeeEditForm';

export default function EmployeeEditPage() {
  const { id } = useParams<{ id: string }>();
  const employeeId = id ? Number(id) : undefined;
  if (!employeeId || Number.isNaN(employeeId)) return null;
  return <EmployeeEditForm id={employeeId} />;
}
