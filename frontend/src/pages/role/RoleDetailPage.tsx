import { useNavigate, useParams } from 'react-router-dom';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import LockRoundedIcon from '@mui/icons-material/LockRounded';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import { usePermission } from '@/shared/hooks/usePermission';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import { FormSection } from '@/shared/ui/GenericForm';
import GenericHeaderDetails, {
  type HeaderDetailField,
} from '@/shared/ui/GenericHeaderDetails';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useGetRoleQuery } from '@/features/role/api/roleApi';
import MenuPermissionMatrix, {
  MATRIX_MENUS,
} from '@/features/role/components/MenuPermissionMatrix';
import type { RoleDetail, RoleFormValues } from '@/features/role/types';
import { getErrorMessage } from '@/shared/api/error';
import {
  DetailRoot,
  MatrixSection,
  SystemBadge,
} from './RoleDetailPage.styles';

/**
 * 권한 상세 페이지. 읽기 전용 — ROLES read 권한만 있어도 진입 가능.
 * 헤더 우측에 [목록으로] + (canWrite 일 때만) [수정] 버튼.
 */
export default function RoleDetailPage() {
  const { id } = useParams<{ id: string }>();
  const roleId = id ? Number(id) : undefined;
  if (!roleId || Number.isNaN(roleId)) return null;
  return <Body id={roleId} />;
}

function Body({ id }: { id: number }) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.ROLES);
  const { data, isLoading, isError, error, refetch } = useGetRoleQuery(id);

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;
  if (!data) return null;

  return (
    <>
      <PageHeaderActions
        actions={[
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
        ]}
      />

      <DetailRoot>
        <GenericHeaderDetails fields={roleInfoFields(data)} />
        <MatrixSection>
          <FormSection
            title="메뉴 권한"
            description="현재 부여된 메뉴별 읽기/쓰기 권한입니다. 수정은 [수정] 버튼으로 진입하세요."
          >
            <MenuPermissionMatrix
              permissions={buildPermissions(data)}
              onChange={() => {/* readonly */}}
              readOnly
              readOnlyMessage={
                data.system
                  ? '시스템 권한입니다. 메뉴 권한이 자동으로 모두 부여됩니다.'
                  : undefined
              }
            />
          </FormSection>
        </MatrixSection>
      </DetailRoot>
    </>
  );
}

function buildPermissions(detail: RoleDetail): RoleFormValues['permissions'] {
  const permissions: RoleFormValues['permissions'] = {};
  for (const m of MATRIX_MENUS) {
    permissions[m] = { canRead: false, canWrite: false };
  }
  for (const p of detail.menuPermissions) {
    permissions[p.menuCode] = { canRead: p.canRead, canWrite: p.canWrite };
  }
  return permissions;
}

function roleInfoFields(d: RoleDetail): HeaderDetailField[] {
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
