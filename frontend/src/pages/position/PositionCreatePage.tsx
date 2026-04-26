import GenericForm from '@/shared/ui/GenericForm';
import {
  positionFormApi,
  positionFormFields,
} from '@/features/position/config/positionFormConfig';

export default function PositionCreatePage() {
  return <GenericForm api={positionFormApi} fields={positionFormFields} />;
}
