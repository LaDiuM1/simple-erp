import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useAfterServiceCreateForm } from '@/features/afterService/hooks/useAfterServiceCreateForm';
import ReceptionSection from '../afterServiceForm/ReceptionSection';
import ProcessSection from '../afterServiceForm/ProcessSection';
import { CreateForm, CreateRoot } from '../afterServiceForm/afterServiceForm.styles';

const FORM_ID = 'after-service-create-form';

export default function AfterServiceCreateForm() {
  const form = useAfterServiceCreateForm();

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'create',
            formId: FORM_ID,
            loading: form.isSaving,
            menuCode: MENU_CODE.AFTER_SERVICES,
          },
        ]}
      />

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <ReceptionSection form={form} mode="create" />
          <ProcessSection form={form} />
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="AS 접수"
        message={`${form.values.customerName || '선택한 고객사'} 의 AS 건을 접수하시겠습니까?`}
        confirmLabel={form.isSaving ? '접수 중...' : '접수'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
