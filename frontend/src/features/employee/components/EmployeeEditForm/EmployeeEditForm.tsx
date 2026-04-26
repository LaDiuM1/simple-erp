import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useGetEmployeeQuery } from '@/features/employee/api/employeeApi';
import { useEmployeeEditForm } from '@/features/employee/hooks/useEmployeeEditForm';
import type { EmployeeDetail } from '@/features/employee/types';
import { getErrorMessage } from '@/shared/api/error';
import BasicInfoSection from '../employeeForm/BasicInfoSection';
import AffiliationSection from '../employeeForm/AffiliationSection';
import AddressSection from '../employeeForm/AddressSection';
import { CreateForm, CreateRoot } from '../employeeForm/employeeForm.styles';
import AccountInfoSection from './AccountInfoSection';

const FORM_ID = 'employee-edit-form';

export default function EmployeeEditForm({ id }: { id: number }) {
  const { data, isLoading, isError, error, refetch } = useGetEmployeeQuery(id);

  if (isLoading) return <LoadingScreen />;
  if (isError) {
    return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;
  }
  if (!data) return null;

  return <EmployeeEditFormBody id={id} detail={data} />;
}

function EmployeeEditFormBody({ id, detail }: { id: number; detail: EmployeeDetail }) {
  const form = useEmployeeEditForm(id, detail);

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'save',
            formId: FORM_ID,
            loading: form.isSaving,
            menuCode: MENU_CODE.EMPLOYEES,
          },
        ]}
      />

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <AccountInfoSection form={form} />
          <BasicInfoSection form={form} />
          <AffiliationSection form={form} showStatus />
          <AddressSection form={form} />
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="직원 수정"
        message={`${form.values.name.trim() || '입력한 직원'} 의 정보를 저장하시겠습니까?`}
        confirmLabel={form.isSaving ? '저장 중...' : '저장'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
