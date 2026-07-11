import GenericDetailModal from '@/shared/ui/GenericDetailModal';
import Muted from '@/shared/ui/atoms/Muted';
import { formatKrw } from '@/shared/utils/formatKrw';
import PaymentFormModal from '@/features/contract/components/PaymentFormModal/PaymentFormModal';
import type { PaymentTabModalProps } from '@/features/contract/hooks/usePaymentTab';
import type { ContractPayment } from '@/features/contract/types';

interface Props {
  modal: PaymentTabModalProps;
}

/** 대금 스케줄 탭의 모달 묶음 — 등록 / 수정 (삭제 포함) 폼 모달 + 읽기 전용 detail 모달. */
export default function PaymentTabModals({ modal }: Props) {
  return (
    <>
      <PaymentFormModal
        open={modal.creating}
        onClose={modal.onCloseCreate}
        contractId={modal.contractId}
      />
      {modal.editing && (
        <PaymentFormModal
          open
          onClose={modal.onCloseEdit}
          contractId={modal.contractId}
          payment={modal.editing}
        />
      )}
      <GenericDetailModal
        open={modal.viewing !== null}
        onClose={modal.onCloseView}
        title={modal.viewing?.label ?? '대금 회차'}
        fields={modal.viewing ? paymentDetailFields(modal.viewing) : []}
      />
    </>
  );
}

function paymentDetailFields(p: ContractPayment) {
  const amount = (v: number | null) => (v == null ? <Muted /> : formatKrw(v));
  return [
    { label: '회차', value: p.label },
    { label: '입금 예정일', value: p.plannedDate },
    { label: '예정 금액', value: amount(p.plannedAmount) },
    { label: '입금일', value: p.paidDate },
    { label: '입금액', value: amount(p.paidAmount) },
    { label: '계산서 발행일', value: p.invoiceDate },
    { label: '계산서 금액', value: amount(p.invoiceAmount) },
    { label: '메모', value: p.note },
  ];
}
