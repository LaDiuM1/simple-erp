import { useParams } from 'react-router-dom';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import AfterServiceEditForm from '@/features/afterService/components/AfterServiceEditForm/AfterServiceEditForm';
import { useAfterServiceEditPage } from '@/features/afterService/hooks/useAfterServiceEditPage';

export default function AfterServiceEditPage() {
  const { id } = useParams<{ id: string }>();
  const afterServiceId = Number(id);
  if (!afterServiceId) return null;

  const { queries } = useAfterServiceEditPage(afterServiceId);

  return (
    <QueryGate queries={queries}>
      {({ detail }) => <AfterServiceEditForm id={afterServiceId} detail={detail} />}
    </QueryGate>
  );
}
