import { useParams } from 'react-router-dom';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import EmployeeEditForm from '@/features/employee/components/EmployeeEditForm/EmployeeEditForm';
import { useEmployeeEditPage } from '@/features/employee/hooks/useEmployeeEditPage';

export default function EmployeeEditPage() {
  const { id } = useParams<{ id: string }>();
  const employeeId = Number(id);
  if (!employeeId) return null;

  const { queries } = useEmployeeEditPage(employeeId);

  return (
    <QueryGate queries={queries}>
      {({ detail }) => <EmployeeEditForm id={employeeId} detail={detail} />}
    </QueryGate>
  );
}
