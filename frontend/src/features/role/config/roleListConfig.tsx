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
  { type: 'search', key: 'codeKeyword', placeholder: '권한 코드 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '권한명 검색' },
];

/**
 * 권한 목록용 ListApiConfig 를 생성하는 훅.
 * 행 클릭 = 상세 페이지 (읽기 권한). 편집 아이콘 = 수정 페이지 (쓰기 권한). 둘이 경쟁하지 않도록 GenericList 가 propagation 을 차단.
 */
export function useRoleListApi(): ListApiConfig<RoleSummary, RoleListFilters> {
  const navigate = useNavigate();
  return {
    menuCode: MENU_CODE.ROLES,
    useList: useGetRolesSummaryQuery,
    useDelete: useDeleteRoleMutation,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.ROLES]}/${m.id}/edit`),
    onRowClick: (m) => navigate(`${MENU_PATH[MENU_CODE.ROLES]}/${m.id}`),
  };
}
