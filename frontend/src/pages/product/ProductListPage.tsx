import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  productListColumns,
  productListFilters,
} from '@/features/product/config/productListConfig';
import { useProductListPage } from '@/features/product/hooks/useProductListPage';

export default function ProductListPage() {
  const { api, headerActions } = useProductListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={productListFilters} column={productListColumns} />
    </>
  );
}
