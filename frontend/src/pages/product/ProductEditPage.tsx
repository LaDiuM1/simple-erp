import { useParams } from 'react-router-dom';
import GenericForm from '@/shared/ui/GenericForm';
import {
  productFormApi,
  productFormFields,
} from '@/features/product/config/productFormConfig';

export default function ProductEditPage() {
  const { id } = useParams<{ id: string }>();
  const productId = id ? Number(id) : undefined;
  if (!productId || Number.isNaN(productId)) return null;
  return <GenericForm api={productFormApi} fields={productFormFields} id={productId} />;
}
