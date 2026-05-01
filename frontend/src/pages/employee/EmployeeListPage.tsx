import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  employeeListColumns,
  employeeListFilters,
} from '@/features/employee/config/employeeListConfig';
import { useEmployeeListPage } from '@/features/employee/hooks/useEmployeeListPage';

export default function EmployeeListPage() {
  const { api, headerActions } = useEmployeeListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={employeeListFilters} column={employeeListColumns} />
    </>
  );
}
