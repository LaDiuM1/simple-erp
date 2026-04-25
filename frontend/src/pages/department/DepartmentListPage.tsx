import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  departmentColumn,
  departmentSearchFilter,
  useDepartmentListApi,
} from '@/features/department/config/departmentListConfig';

export default function DepartmentListPage() {
  const navigate = useNavigate();
  const api = useDepartmentListApi();

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'create',
            label: '부서 등록',
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.DEPARTMENTS]}/new`),
            menuCode: MENU_CODE.DEPARTMENTS,
          },
        ]}
      />

      <GenericList api={api} searchFilter={departmentSearchFilter} column={departmentColumn} />
    </>
  );
}
