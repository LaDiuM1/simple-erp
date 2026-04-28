import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import RouteRoundedIcon from '@mui/icons-material/RouteRounded';
import { MENU_CODE } from '@/shared/config/menuConfig';
import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  salesContactColumn,
  salesContactSearchFilter,
  useSalesContactListApi,
} from '@/features/salesContact/config/salesContactListConfig';
import AcquisitionSourceManageModal from '@/features/acquisitionSource/components/AcquisitionSourceManageModal';

export default function SalesContactListPage() {
  const navigate = useNavigate();
  const api = useSalesContactListApi();
  const [manageOpen, setManageOpen] = useState(false);

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'secondary',
            label: '컨택 경로 관리',
            icon: <RouteRoundedIcon />,
            onClick: () => setManageOpen(true),
          },
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
      <AcquisitionSourceManageModal open={manageOpen} onClose={() => setManageOpen(false)} />
    </>
  );
}
