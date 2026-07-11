import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  supplierListColumns,
  supplierListFilters,
} from '@/features/supplier/config/supplierListConfig';
import { useSupplierListPage } from '@/features/supplier/hooks/useSupplierListPage';

export default function SupplierListPage() {
  const { api, headerActions } = useSupplierListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={supplierListFilters} column={supplierListColumns} />
    </>
  );
}
