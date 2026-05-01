import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useCustomerEditForm } from '@/features/customer/hooks/useCustomerEditForm';
import type { CustomerDetail } from '@/features/customer/types';
import IdentitySection from '../customerForm/IdentitySection';
import ClassificationSection from '../customerForm/ClassificationSection';
import ContactSection from '../customerForm/ContactSection';
import AddressSection from '../customerForm/AddressSection';
import { CreateForm, CreateRoot } from '../customerForm/customerForm.styles';

const FORM_ID = 'customer-edit-form';

/**
 * 고객사 수정 폼 Body — outer (page) 가 detail 보장한 뒤 위임. form-state hook 의 invariant 충족.
 */
export default function CustomerEditForm({ id, detail }: { id: number; detail: CustomerDetail }) {
  const form = useCustomerEditForm(id, detail);

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'save',
            formId: FORM_ID,
            loading: form.isSaving,
            menuCode: MENU_CODE.CUSTOMERS,
          },
        ]}
      />

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <IdentitySection form={form} mode="edit" />
          <ClassificationSection form={form} />
          <ContactSection form={form} />
          <AddressSection form={form} />
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="고객사 수정"
        message={`${form.values.name.trim() || '입력한 고객사'}의 정보를 저장하시겠습니까?`}
        confirmLabel={form.isSaving ? '저장 중...' : '저장'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
