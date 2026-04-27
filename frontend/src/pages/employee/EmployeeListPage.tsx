import { useNavigate } from 'react-router-dom';
import { MENU_CODE } from '@/shared/config/menuConfig';
import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  employeeColumn,
  employeeSearchFilter,
  useEmployeeListApi,
} from '@/features/employee/config/employeeListConfig';

export default function EmployeeListPage() {
  const navigate = useNavigate();
  const api = useEmployeeListApi();

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'create',
            label: '직원 등록',
            onClick: () => navigate('/employees/new'),
            menuCode: MENU_CODE.EMPLOYEES,
          },
        ]}
      />

      <GenericList
        api={api}
        searchFilter={employeeSearchFilter}
        column={employeeColumn}
      />
    </>
  );
}
