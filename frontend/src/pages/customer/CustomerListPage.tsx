import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  customerListColumns,
  customerListFilters,
} from '@/features/customer/config/customerListConfig';
import { useCustomerListPage } from '@/features/customer/hooks/useCustomerListPage';

export default function CustomerListPage() {
  const { api, headerActions } = useCustomerListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={customerListFilters} column={customerListColumns} />
    </>
  );
}
