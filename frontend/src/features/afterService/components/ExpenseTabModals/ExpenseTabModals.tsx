import GenericDetailModal from '@/shared/ui/GenericDetailModal';
import { formatKrw } from '@/shared/utils/formatKrw';
import ExpenseFormModal from '@/features/afterService/components/ExpenseFormModal/ExpenseFormModal';
import type { ExpenseTabModalProps } from '@/features/afterService/hooks/useExpenseTab';
import {
  EXPENSE_PAYER_TYPE_LABELS,
  SERVICE_EXPENSE_CATEGORY_LABELS,
  type ServiceExpense,
} from '@/features/afterService/types';

interface Props {
  modal: ExpenseTabModalProps;
}

/** 경비 탭의 모달 묶음 — 등록 / 수정 (삭제 포함) 폼 모달 + 읽기 전용 detail 모달. */
export default function ExpenseTabModals({ modal }: Props) {
  return (
    <>
      <ExpenseFormModal
        open={modal.creating}
        onClose={modal.onCloseCreate}
        afterServiceId={modal.afterServiceId}
      />
      {modal.editing && (
        <ExpenseFormModal
          open
          onClose={modal.onCloseEdit}
          afterServiceId={modal.afterServiceId}
          expense={modal.editing}
        />
      )}
      <GenericDetailModal
        open={modal.viewing !== null}
        onClose={modal.onCloseView}
        title={
          modal.viewing
            ? `경비 — ${SERVICE_EXPENSE_CATEGORY_LABELS[modal.viewing.category]}`
            : '경비'
        }
        fields={modal.viewing ? expenseDetailFields(modal.viewing) : []}
      />
    </>
  );
}

function expenseDetailFields(e: ServiceExpense) {
  return [
    { label: '분류', value: SERVICE_EXPENSE_CATEGORY_LABELS[e.category] },
    { label: '금액', value: formatKrw(e.amount) },
    { label: '결제 주체', value: EXPENSE_PAYER_TYPE_LABELS[e.payerType] },
    { label: '결제일', value: e.paidDate },
    { label: '엔지니어', value: e.engineerName },
    { label: '메모', value: e.note },
  ];
}
