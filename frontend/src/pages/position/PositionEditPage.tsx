import { useParams } from 'react-router-dom';
import GenericForm from '@/shared/ui/GenericForm';
import {
  positionFormApi,
  positionFormFields,
} from '@/features/position/config/positionFormConfig';

export default function PositionEditPage() {
  const { id } = useParams<{ id: string }>();
  const positionId = id ? Number(id) : undefined;
  if (!positionId || Number.isNaN(positionId)) return null;
  return <GenericForm api={positionFormApi} fields={positionFormFields} id={positionId} />;
}
