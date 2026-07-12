import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useAfterServiceEditForm } from '@/features/afterService/hooks/useAfterServiceEditForm';
import type { AfterServiceDetail } from '@/features/afterService/types';
import ReceptionSection from '../afterServiceForm/ReceptionSection';
import ProcessSection from '../afterServiceForm/ProcessSection';
import { CreateForm, CreateRoot } from '../afterServiceForm/afterServiceForm.styles';

const FORM_ID = 'after-service-edit-form';

/**
 * AS 수정 폼 Body — outer (page) 가 detail 보장한 뒤 위임. form-state hook 의 invariant 충족.
 */
export default function AfterServiceEditForm({
  id,
  detail,
}: {
  id: number;
  detail: AfterServiceDetail;
}) {
  const form = useAfterServiceEditForm(id, detail);

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'save',
            formId: FORM_ID,
            loading: form.isSaving,
            menuCode: MENU_CODE.AFTER_SERVICES,
          },
        ]}
      />

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <ReceptionSection form={form} mode="edit" />
          <ProcessSection form={form} />
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="AS 수정"
        message={`${form.values.receiptNo} 건의 정보를 저장하시겠습니까?`}
        confirmLabel={form.isSaving ? '저장 중...' : '저장'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
