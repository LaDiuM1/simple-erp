import GenericForm from '@/shared/ui/GenericForm';
import {
  productFormApi,
  productFormFields,
} from '@/features/product/config/productFormConfig';

export default function ProductCreatePage() {
  return <GenericForm api={productFormApi} fields={productFormFields} />;
}
