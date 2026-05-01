import { useNavigate } from 'react-router-dom';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import type { ListApiConfig } from '@/shared/ui/GenericList';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import {
  useDeleteRoleMutation,
  useDeleteRolesMutation,
  useGetRolesSummaryQuery,
} from '@/features/role/api/roleApi';
import {
  type RoleListFilters,
  type RoleSummary,
} from '@/features/role/types';

/**
 * 권한 목록 page hook — api + headerActions 묶음.
 */
export function useRoleListPage(): {
  api: ListApiConfig<RoleSummary, RoleListFilters>;
  headerActions: PageHeaderAction[];
} {
  const navigate = useNavigate();

  const api: ListApiConfig<RoleSummary, RoleListFilters> = {
    menuCode: MENU_CODE.ROLES,
    useList: useGetRolesSummaryQuery,
    useDelete: useDeleteRoleMutation,
    useBulkDelete: useDeleteRolesMutation,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.ROLES]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.ROLES]}/${m.id}`),
  };

  const headerActions: PageHeaderAction[] = [
    {
      design: 'create',
      label: '권한 등록',
      onClick: () => navigate(`${MENU_PATH[MENU_CODE.ROLES]}/new`),
      menuCode: MENU_CODE.ROLES,
    },
  ];

  return { api, headerActions };
}
