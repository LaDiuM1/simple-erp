import GenericDetailModal, { type DetailModalField } from '@/shared/ui/GenericDetailModal';
import type { SalesActivity } from '@/features/salesCustomer/types';

export interface ActivityTabModalProps {
  detailTarget: SalesActivity | null;
  detailFields: DetailModalField[];
  onCloseDetail: () => void;
}

export default function ActivityTabModals({ modal }: { modal: ActivityTabModalProps }) {
  return (
    <GenericDetailModal
      open={modal.detailTarget !== null}
      onClose={modal.onCloseDetail}
      title={modal.detailTarget?.subject ?? '영업 활동'}
      fields={modal.detailFields}
    />
  );
}
