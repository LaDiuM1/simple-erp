import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  salesCustomerListColumns,
  salesCustomerListFilters,
} from '@/features/salesCustomer/config/salesCustomerListConfig';
import { useSalesCustomerListPage } from '@/features/salesCustomer/hooks/useSalesCustomerListPage';

export default function SalesCustomerListPage() {
  const { api, headerActions } = useSalesCustomerListPage();
  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={salesCustomerListFilters} column={salesCustomerListColumns} />
    </>
  );
}
