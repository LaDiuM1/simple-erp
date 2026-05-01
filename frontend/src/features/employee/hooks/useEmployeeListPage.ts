import { useNavigate } from 'react-router-dom';
import { MENU_CODE } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useDeleteEmployeeMutation,
  useDeleteEmployeesMutation,
  useDownloadEmployeesExcel,
  useGetEmployeesQuery,
} from '@/features/employee/api/employeeApi';
import {
  type EmployeeListFilters,
  type EmployeeSummary,
} from '@/features/employee/types';

/**
 * 직원 목록 page hook — api + headerActions 묶음.
 */
export function useEmployeeListPage(): {
  api: ListApiConfig<EmployeeSummary, EmployeeListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<EmployeeSummary, EmployeeListFilters> = {
    menuCode: MENU_CODE.EMPLOYEES,
    useList: useGetEmployeesQuery,
    useDelete: useDeleteEmployeeMutation,
    useBulkDelete: useDeleteEmployeesMutation,
    useExcel: useDownloadEmployeesExcel,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`/employees/${m.id}/edit`),
    onRowClick: (m) => navigate(`/employees/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'create',
      label: '직원 등록',
      onClick: () => navigate('/employees/new'),
      menuCode: MENU_CODE.EMPLOYEES,
    },
  ];

  return { api, headerActions };
}
