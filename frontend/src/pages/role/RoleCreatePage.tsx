import { useState } from 'react';
import RoleForm from '@/features/role/components/RoleForm';
import { useRoleCreateForm } from '@/features/role/hooks/useRoleCreateForm';

export default function RoleCreatePage() {
  const [codeAvailable, setCodeAvailable] = useState<boolean | null>(null);
  const form = useRoleCreateForm({ codeAvailable });
  return <RoleForm mode="create" setCodeAvailable={setCodeAvailable} {...form} />;
}
