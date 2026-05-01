import { useRoleEditForm } from '@/features/role/hooks/useRoleEditForm';
import RoleForm from '@/features/role/components/RoleForm';
import type { RoleDetail } from '@/features/role/types';

/**
 * 권한 수정 폼 Body — outer (page) 가 detail 보장한 뒤 위임. form-state hook 의 invariant 충족.
 */
export default function RoleEditForm({ id, detail }: { id: number; detail: RoleDetail }) {
  const form = useRoleEditForm(id, detail);
  return <RoleForm mode="edit" {...form} />;
}
