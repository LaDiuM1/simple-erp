import GenericForm from '@/shared/ui/GenericForm';
import {
  supplierFormApi,
  supplierFormFields,
} from '@/features/supplier/config/supplierFormConfig';

export default function SupplierCreatePage() {
  return <GenericForm api={supplierFormApi} fields={supplierFormFields} />;
}
