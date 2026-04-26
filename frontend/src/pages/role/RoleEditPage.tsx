import { useParams } from 'react-router-dom';
import RoleEditForm from '@/features/role/components/RoleEditForm';

export default function RoleEditPage() {
  const { id } = useParams<{ id: string }>();
  const roleId = id ? Number(id) : undefined;
  if (!roleId || Number.isNaN(roleId)) return null;
  return <RoleEditForm id={roleId} />;
}
