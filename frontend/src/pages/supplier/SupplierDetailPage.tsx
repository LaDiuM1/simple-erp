import { useParams } from 'react-router-dom';
import GenericForm from '@/shared/ui/GenericForm';
import {
  supplierFormApi,
  supplierFormFields,
} from '@/features/supplier/config/supplierFormConfig';

export default function SupplierDetailPage() {
  const { id } = useParams<{ id: string }>();
  const supplierId = id ? Number(id) : undefined;
  if (!supplierId || Number.isNaN(supplierId)) return null;
  return <GenericForm api={supplierFormApi} fields={supplierFormFields} id={supplierId} readOnly />;
}
