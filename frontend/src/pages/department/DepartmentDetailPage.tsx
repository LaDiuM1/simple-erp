import { useParams } from 'react-router-dom';
import GenericForm from '@/shared/ui/GenericForm';
import {
  departmentFormApi,
  departmentFormFields,
} from '@/features/department/config/departmentFormConfig';

export default function DepartmentDetailPage() {
  const { id } = useParams<{ id: string }>();
  const departmentId = id ? Number(id) : undefined;
  if (!departmentId || Number.isNaN(departmentId)) return null;
  return <GenericForm api={departmentFormApi} fields={departmentFormFields} id={departmentId} readOnly />;
}
