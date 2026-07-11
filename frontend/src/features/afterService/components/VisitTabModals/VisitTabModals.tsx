import GenericDetailModal from '@/shared/ui/GenericDetailModal';
import VisitFormModal from '@/features/afterService/components/VisitFormModal/VisitFormModal';
import type { VisitTabModalProps } from '@/features/afterService/hooks/useVisitTab';
import type { ServiceVisit } from '@/features/afterService/types';

interface Props {
  modal: VisitTabModalProps;
}

/** 방문 일지 탭의 모달 묶음 — 등록 / 수정 (삭제 포함) 폼 모달 + 읽기 전용 detail 모달. */
export default function VisitTabModals({ modal }: Props) {
  return (
    <>
      <VisitFormModal
        open={modal.creating}
        onClose={modal.onCloseCreate}
        afterServiceId={modal.afterServiceId}
      />
      {modal.editing && (
        <VisitFormModal
          open
          onClose={modal.onCloseEdit}
          afterServiceId={modal.afterServiceId}
          visit={modal.editing}
        />
      )}
      <GenericDetailModal
        open={modal.viewing !== null}
        onClose={modal.onCloseView}
        title={modal.viewing ? `방문 일지 — ${modal.viewing.visitDate}` : '방문 일지'}
        fields={modal.viewing ? visitDetailFields(modal.viewing) : []}
      />
    </>
  );
}

function visitDetailFields(v: ServiceVisit) {
  return [
    { label: '방문일', value: v.visitDate },
    { label: '엔지니어', value: v.engineerName },
    { label: '문제', value: v.problem },
    { label: '해결', value: v.resolution },
  ];
}
