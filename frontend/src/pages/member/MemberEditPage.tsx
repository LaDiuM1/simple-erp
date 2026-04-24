import { useParams } from 'react-router-dom';
import GenericForm from '@/shared/ui/GenericForm';
import { memberFormApi, memberFormFields } from '@/features/member/config/memberFormConfig';

export default function MemberEditPage() {
  const { id } = useParams<{ id: string }>();
  const memberId = id ? Number(id) : undefined;
  if (!memberId || Number.isNaN(memberId)) return null;

  return <GenericForm api={memberFormApi} fields={memberFormFields} id={memberId} />;
}
