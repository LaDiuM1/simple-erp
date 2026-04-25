import { useNavigate } from 'react-router-dom';
import AddIcon from '@mui/icons-material/Add';
import { MENU_CODE } from '@/shared/config/menuConfig';
import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { PrimaryPageHeaderButton } from '@/shared/ui/layout/PageHeaderButton';
import PermissionGate from '@/shared/ui/layout/PermissionGate';
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
      <PageHeaderActions>
        <PermissionGate menuCode={MENU_CODE.EMPLOYEES} action="write">
          <PrimaryPageHeaderButton
            startIcon={<AddIcon />}
            onClick={() => navigate('/employees/new')}
          >
            직원 등록
          </PrimaryPageHeaderButton>
        </PermissionGate>
      </PageHeaderActions>

      <GenericList api={api} searchFilter={employeeSearchFilter} column={employeeColumn} />
    </>
  );
}
