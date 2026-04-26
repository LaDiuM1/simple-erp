import { useParams } from 'react-router-dom';
import GenericForm from '@/shared/ui/GenericForm';
import {
  salesContactFormApi,
  salesContactFormFields,
} from '@/features/salesContact/config/salesContactFormConfig';

export default function SalesContactEditPage() {
  const { id } = useParams<{ id: string }>();
  const contactId = id ? Number(id) : undefined;
  if (!contactId || Number.isNaN(contactId)) return null;
  return <GenericForm api={salesContactFormApi} fields={salesContactFormFields} id={contactId} />;
}
