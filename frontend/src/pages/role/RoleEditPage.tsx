import { useParams } from 'react-router-dom';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import RoleEditForm from '@/features/role/components/RoleEditForm';
import { useRoleEditPage } from '@/features/role/hooks/useRoleEditPage';

export default function RoleEditPage() {
  const { id } = useParams<{ id: string }>();
  const roleId = Number(id);
  if (!roleId) return null;

  const { queries } = useRoleEditPage(roleId);

  return (
    <QueryGate queries={queries}>
      {({ detail }) => <RoleEditForm id={roleId} detail={detail} />}
    </QueryGate>
  );
}
