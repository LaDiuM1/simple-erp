import GenericDetailModal, { type DetailModalField } from '@/shared/ui/GenericDetailModal';
import type { ExpenseItem } from '@/features/expense/types';

export interface ExpenseItemTabModalProps {
  detailTarget: ExpenseItem | null;
  detailFields: DetailModalField[];
  onCloseDetail: () => void;
}

export default function ExpenseItemTabModals({ modal }: { modal: ExpenseItemTabModalProps }) {
  return (
    <GenericDetailModal
      open={modal.detailTarget !== null}
      onClose={modal.onCloseDetail}
      title="경비 항목"
      fields={modal.detailFields}
    />
  );
}
