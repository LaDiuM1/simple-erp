import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useEmployeeCreateForm } from '@/features/employee/hooks/useEmployeeCreateForm';
import BasicInfoSection from '../employeeForm/BasicInfoSection';
import AffiliationSection from '../employeeForm/AffiliationSection';
import AddressSection from '../employeeForm/AddressSection';
import { CreateForm, CreateRoot } from '../employeeForm/employeeForm.styles';
import AccountSection from './AccountSection';

const FORM_ID = 'employee-create-form';

export default function EmployeeCreateForm() {
  const form = useEmployeeCreateForm();

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'create',
            formId: FORM_ID,
            loading: form.isSaving,
            menuCode: MENU_CODE.EMPLOYEES,
          },
        ]}
      />

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <AccountSection form={form} />
          <BasicInfoSection form={form} />
          <AffiliationSection form={form} />
          <AddressSection form={form} />
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="직원 등록"
        message={`${form.values.name.trim() || '입력한 직원'} 을(를) 등록하시겠습니까?`}
        confirmLabel={form.isSaving ? '등록 중...' : '등록'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
