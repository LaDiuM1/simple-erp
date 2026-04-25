import { useParams } from 'react-router-dom';
import GenericForm from '@/shared/ui/GenericForm';
import { employeeFormApi, employeeFormFields } from '@/features/employee/config/employeeFormConfig';

export default function EmployeeEditPage() {
  const { id } = useParams<{ id: string }>();
  const employeeId = id ? Number(id) : undefined;
  if (!employeeId || Number.isNaN(employeeId)) return null;

  return <GenericForm api={employeeFormApi} fields={employeeFormFields} id={employeeId} />;
}
