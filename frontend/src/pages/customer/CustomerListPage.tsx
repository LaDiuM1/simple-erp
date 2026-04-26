import { useNavigate } from 'react-router-dom';
import { MENU_CODE } from '@/shared/config/menuConfig';
import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  customerColumn,
  customerSearchFilter,
  useCustomerListApi,
} from '@/features/customer/config/customerListConfig';

export default function CustomerListPage() {
  const navigate = useNavigate();
  const api = useCustomerListApi();

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'create',
            label: '고객사 등록',
            onClick: () => navigate('/customers/new'),
            menuCode: MENU_CODE.CUSTOMERS,
          },
        ]}
      />

      <GenericList
        api={api}
        searchFilter={customerSearchFilter}
        column={customerColumn}
      />
    </>
  );
}
