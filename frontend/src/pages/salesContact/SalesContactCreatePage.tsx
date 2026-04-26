import GenericForm from '@/shared/ui/GenericForm';
import {
  salesContactFormApi,
  salesContactFormFields,
} from '@/features/salesContact/config/salesContactFormConfig';

export default function SalesContactCreatePage() {
  return <GenericForm api={salesContactFormApi} fields={salesContactFormFields} />;
}
