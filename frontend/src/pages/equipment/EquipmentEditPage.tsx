import { useParams } from 'react-router-dom';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import EquipmentEditForm from '@/features/equipment/components/EquipmentEditForm/EquipmentEditForm';
import { useEquipmentEditPage } from '@/features/equipment/hooks/useEquipmentEditPage';

export default function EquipmentEditPage() {
  const { id } = useParams<{ id: string }>();
  const equipmentId = Number(id);
  return equipmentId ? <EquipmentEditContent equipmentId={equipmentId} /> : null;
}

function EquipmentEditContent({ equipmentId }: { equipmentId: number }) {
  const { queries } = useEquipmentEditPage(equipmentId);

  return (
    <QueryGate queries={queries}>
      {({ detail }) => <EquipmentEditForm id={equipmentId} detail={detail} />}
    </QueryGate>
  );
}
