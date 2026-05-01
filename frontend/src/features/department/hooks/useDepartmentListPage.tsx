import { useNavigate } from 'react-router-dom';
import AccountTreeRoundedIcon from '@mui/icons-material/AccountTreeRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useDeleteDepartmentMutation,
  useDeleteDepartmentsMutation,
  useGetDepartmentsSummaryQuery,
} from '@/features/department/api/departmentApi';
import {
  type DepartmentListFilters,
  type DepartmentSummary,
} from '@/features/department/types';

/**
 * 부서 목록 page hook — api + headerActions 묶음. 페이지는 destructure 후 GenericList 에 prop 전달.
 */
export function useDepartmentListPage(): {
  api: ListApiConfig<DepartmentSummary, DepartmentListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<DepartmentSummary, DepartmentListFilters> = {
    menuCode: MENU_CODE.DEPARTMENTS,
    useList: useGetDepartmentsSummaryQuery,
    useDelete: useDeleteDepartmentMutation,
    useBulkDelete: useDeleteDepartmentsMutation,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.DEPARTMENTS]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.DEPARTMENTS]}/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'secondary',
      label: '부서 계층 관리',
      icon: <AccountTreeRoundedIcon />,
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.DEPARTMENTS]}/hierarchy`),
    },
    {
      design: 'create',
      label: '부서 등록',
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.DEPARTMENTS]}/new`),
      menuCode: MENU_CODE.DEPARTMENTS,
    },
  ];

  return { api, headerActions };
}
