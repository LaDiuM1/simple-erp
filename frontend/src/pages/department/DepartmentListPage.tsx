import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  departmentListColumns,
  departmentListFilters,
} from '@/features/department/config/departmentListConfig';
import { useDepartmentListPage } from '@/features/department/hooks/useDepartmentListPage';

export default function DepartmentListPage() {
  const { api, headerActions } = useDepartmentListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={departmentListFilters} column={departmentListColumns} />
    </>
  );
}
