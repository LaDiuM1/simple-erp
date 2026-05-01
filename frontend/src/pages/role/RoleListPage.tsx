import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  roleListColumns,
  roleListFilters,
} from '@/features/role/config/roleListConfig';
import { useRoleListPage } from '@/features/role/hooks/useRoleListPage';

export default function RoleListPage() {
  const { api, headerActions } = useRoleListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={roleListFilters} column={roleListColumns} />
    </>
  );
}
