import { useParams } from 'react-router-dom';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import { FormSection } from '@/shared/ui/GenericForm';
import GenericHeaderDetails from '@/shared/ui/GenericHeaderDetails';
import MenuPermissionMatrix from '@/features/role/components/MenuPermissionMatrix';
import {
  buildPermissions,
  roleInfoFields,
  useRoleDetailPage,
} from '@/features/role/hooks/useRoleDetailPage';
import { DetailRoot, MatrixSection } from './RoleDetailPage.styles';

/**
 * 권한 상세 페이지. 읽기 전용 — ROLES read 권한만 있어도 진입 가능.
 */
export default function RoleDetailPage() {
  const { id } = useParams<{ id: string }>();
  const roleId = Number(id);
  if (!roleId) return null;

  const { queries, headerActions } = useRoleDetailPage(roleId);

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ detail }) => (
          <DetailRoot>
            <GenericHeaderDetails fields={roleInfoFields(detail)} />
            <MatrixSection>
              <FormSection
                title="메뉴 권한"
                description="현재 부여된 메뉴별 읽기/쓰기 권한입니다. 수정은 [수정] 버튼으로 진입하세요."
              >
                <MenuPermissionMatrix
                  permissions={buildPermissions(detail)}
                  onChange={() => {/* readonly */}}
                  readOnly
                  readOnlyMessage={
                    detail.system
                      ? '시스템 권한입니다. 메뉴 권한이 자동으로 모두 부여됩니다.'
                      : undefined
                  }
                />
              </FormSection>
            </MatrixSection>
          </DetailRoot>
        )}
      </QueryGate>
    </>
  );
}
