import { useParams } from 'react-router-dom';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import EmployeeDetailForm from '@/features/employee/components/EmployeeDetailForm/EmployeeDetailForm';
import { useEmployeeDetailPage } from '@/features/employee/hooks/useEmployeeDetailPage';

export default function EmployeeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const employeeId = Number(id);
  if (!employeeId) return null;

  const { queries } = useEmployeeDetailPage(employeeId);

  return (
    <QueryGate queries={queries}>
      {({ detail }) => <EmployeeDetailForm id={employeeId} detail={detail} />}
    </QueryGate>
  );
}
