import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  roleColumn,
  roleSearchFilter,
  useRoleListApi,
} from '@/features/role/config/roleListConfig';

export default function RoleListPage() {
  const navigate = useNavigate();
  const api = useRoleListApi();

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'create',
            label: '권한 등록',
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.ROLES]}/new`),
            menuCode: MENU_CODE.ROLES,
          },
        ]}
      />

      <GenericList
        api={api}
        searchFilter={roleSearchFilter}
        column={roleColumn}
      />
    </>
  );
}
