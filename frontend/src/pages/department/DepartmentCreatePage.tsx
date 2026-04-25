import GenericForm from '@/shared/ui/GenericForm';
import {
  departmentFormApi,
  departmentFormFields,
} from '@/features/department/config/departmentFormConfig';

export default function DepartmentCreatePage() {
  return <GenericForm api={departmentFormApi} fields={departmentFormFields} />;
}
