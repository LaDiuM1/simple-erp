import { useParams } from 'react-router-dom';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import GenericHeaderDetails from '@/shared/ui/GenericHeaderDetails';
import {
  equipmentInfoFields,
  useEquipmentDetailPage,
} from '@/features/equipment/hooks/useEquipmentDetailPage';
import { DetailRoot } from '@/features/equipment/components/equipmentDetail.styles';

export default function EquipmentDetailPage() {
  const { id } = useParams<{ id: string }>();
  const equipmentId = Number(id);
  if (!equipmentId) return null;

  const { queries, headerActions } = useEquipmentDetailPage(equipmentId);

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ detail }) => (
          <DetailRoot>
            <GenericHeaderDetails fields={equipmentInfoFields(detail)} />
          </DetailRoot>
        )}
      </QueryGate>
    </>
  );
}
