import { useNavigate } from 'react-router-dom';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import Typography from '@mui/material/Typography';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import {
  type ColumnConfig,
  type FilterConfig,
  type ListApiConfig,
} from '@/shared/ui/GenericList';
import {
  useDeleteRoleMutation,
  useGetRolesSummaryQuery,
} from '@/features/role/api/roleApi';
import {
  type RoleListFilters,
  type RoleSummary,
} from '@/features/role/types';

export const roleColumn: ColumnConfig<RoleSummary>[] = [
  {
    key: 'code',
    label: '권한 코드',
    sortable: true,
    sortDirection: 'asc',
    defaultSort: true,
    mobilePrimary: true,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary', display: 'inline-flex', alignItems: 'center', gap: '0.375rem' }}>
        {m.code}
        {m.system && (
          <LockRoundedIcon sx={{ fontSize: 14, color: 'text.disabled' }} titleAccess="시스템 권한" />
        )}
      </Typography>
    ),
  },
  { key: 'name', label: '권한명', sortable: true, sortDirection: 'asc' },
  { key: 'description', label: '설명' },
];

export const roleSearchFilter: FilterConfig[] = [
  { type: 'search', key: 'keyword', placeholder: '권한 코드 또는 권한명 검색' },
];

/**
 * 권한 목록용 ListApiConfig 를 생성하는 훅.
 */
export function useRoleListApi(): ListApiConfig<RoleSummary, RoleListFilters> {
  const navigate = useNavigate();
  return {
    menuCode: MENU_CODE.ROLES,
    useList: useGetRolesSummaryQuery,
    useDelete: useDeleteRoleMutation,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.ROLES]}/${m.id}/edit`),
  };
}
