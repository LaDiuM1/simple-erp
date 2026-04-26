import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useCustomerCreateForm } from '@/features/customer/hooks/useCustomerCreateForm';
import IdentitySection from '../customerForm/IdentitySection';
import ClassificationSection from '../customerForm/ClassificationSection';
import ContactSection from '../customerForm/ContactSection';
import AddressSection from '../customerForm/AddressSection';
import { CreateForm, CreateRoot } from '../customerForm/customerForm.styles';

const FORM_ID = 'customer-create-form';

export default function CustomerCreateForm() {
  const form = useCustomerCreateForm();

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'create',
            formId: FORM_ID,
            loading: form.isSaving,
            menuCode: MENU_CODE.CUSTOMERS,
          },
        ]}
      />

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <IdentitySection form={form} mode="create" />
          <ClassificationSection form={form} />
          <ContactSection form={form} />
          <AddressSection form={form} />
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="고객사 등록"
        message={`${form.values.name.trim() || '입력한 고객사'} 을(를) 등록하시겠습니까?`}
        confirmLabel={form.isSaving ? '등록 중...' : '등록'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
