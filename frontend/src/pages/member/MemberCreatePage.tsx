import GenericForm from '@/shared/ui/GenericForm';
import { memberFormApi, memberFormFields } from '@/features/member/config/memberFormConfig';

export default function MemberCreatePage() {
  return <GenericForm api={memberFormApi} fields={memberFormFields} />;
}
