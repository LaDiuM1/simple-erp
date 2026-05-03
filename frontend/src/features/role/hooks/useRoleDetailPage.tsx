import { useNavigate } from 'react-router-dom';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import type { PageHeaderAction } from '@/shared/ui/layout/PageHeaderActions';
import type { HeaderDetailField } from '@/shared/ui/GenericHeaderDetails';
import { useGetRoleQuery } from '@/features/role/api/roleApi';
import { MATRIX_MENUS } from '@/features/role/components/MenuPermissionMatrix';
import type { RoleDetail, RoleFormValues } from '@/features/role/types';
import { SystemBadge } from '@/pages/role/RoleDetailPage.styles';

/**
 * 권한 상세 page hook — fetching / 권한 / headerActions 묶음 + buildPermissions 헬퍼.
 * Hook 은 JSX 반환하지 않는다 (CLAUDE.md). roleInfoFields 는 detail 보장된 시점에 page 가 호출.
 */
export function useRoleDetailPage(id: number) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.ROLES);
  const detailQuery = useGetRoleQuery(id, { skip: !id });

  const headerActions: PageHeaderAction[] = [
    {
      design: 'cancel',
      label: '목록으로',
      onClick: () => navigate(MENU_PATH[MENU_CODE.ROLES]),
    },
    ...(canWrite
      ? [
          {
            design: 'create' as const,
            label: '수정',
            icon: <EditOutlinedIcon />,
            onClick: () => navigate(`${MENU_PATH[MENU_CODE.ROLES]}/${id}/edit`),
            menuCode: MENU_CODE.ROLES,
          },
        ]
      : []),
  ];

  return {
    queries: { detail: detailQuery },
    headerActions,
  };
}

export function buildPermissions(detail: RoleDetail): RoleFormValues['permissions'] {
  const permissions: RoleFormValues['permissions'] = {};
  for (const m of MATRIX_MENUS) {
    permissions[m] = { canRead: false, canWrite: false, dataScope: 'ALL' };
  }
  for (const p of detail.menuPermissions) {
    permissions[p.menuCode] = { canRead: p.canRead, canWrite: p.canWrite, dataScope: p.dataScope };
  }
  return permissions;
}

export function roleInfoFields(d: RoleDetail): HeaderDetailField[] {
  return [
    {
      label: '권한 코드',
      value: d.system ? (
        <>
          {d.code}
          <SystemBadge>
            <LockRoundedIcon sx={{ fontSize: 12 }} />
            시스템 권한
          </SystemBadge>
        </>
      ) : (
        d.code
      ),
    },
    { label: '권한명', value: d.name },
    { label: '설명', value: d.description, fullWidth: true },
  ];
}
