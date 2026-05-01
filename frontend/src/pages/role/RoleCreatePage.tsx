import RoleForm from '@/features/role/components/RoleForm';
import { useRoleCreateForm } from '@/features/role/hooks/useRoleCreateForm';

export default function RoleCreatePage() {
  const form = useRoleCreateForm();
  return <RoleForm mode="create" {...form} />;
}
