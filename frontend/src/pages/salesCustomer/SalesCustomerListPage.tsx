import GenericList from '@/shared/ui/GenericList';
import {
  salesCustomerListColumn,
  salesCustomerListSearchFilter,
  useSalesCustomerListApi,
} from '@/features/salesCustomer/config/salesCustomerListConfig';

export default function SalesCustomerListPage() {
  const api = useSalesCustomerListApi();
  return (
    <GenericList
      api={api}
      searchFilter={salesCustomerListSearchFilter}
      column={salesCustomerListColumn}
    />
  );
}
