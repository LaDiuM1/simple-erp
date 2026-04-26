import { useNavigate } from 'react-router-dom';
import { MENU_CODE } from '@/shared/config/menuConfig';
import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  salesContactColumn,
  salesContactSearchFilter,
  useSalesContactListApi,
} from '@/features/salesContact/config/salesContactListConfig';

export default function SalesContactListPage() {
  const navigate = useNavigate();
  const api = useSalesContactListApi();
  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'create',
            label: '명부 등록',
            onClick: () => navigate('/sales-contacts/new'),
            menuCode: MENU_CODE.SALES_CONTACTS,
          },
        ]}
      />
      <GenericList
        api={api}
        searchFilter={salesContactSearchFilter}
        column={salesContactColumn}
      />
    </>
  );
}
