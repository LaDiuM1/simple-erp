import CircularProgress from '@mui/material/CircularProgress';
import SaveIcon from '@mui/icons-material/Save';
import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import { CancelHeaderButton } from '@/shared/ui/GenericForm/GenericForm.styles';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { PrimaryPageHeaderButton } from '@/shared/ui/layout/PageHeaderButton';
import PermissionGate from '@/shared/ui/layout/PermissionGate';
import { useGetEmployeeQuery } from '@/features/employee/api/employeeApi';
import { useEmployeeEditForm } from '@/features/employee/hooks/useEmployeeEditForm';
import type { EmployeeDetail } from '@/features/employee/types';
import type { ApiError } from '@/shared/types/api';
import EmployeeFormHero from '../employeeForm/EmployeeFormHero';
import BasicInfoSection from '../employeeForm/BasicInfoSection';
import AffiliationSection from '../employeeForm/AffiliationSection';
import AddressSection from '../employeeForm/AddressSection';
import { CreateForm, CreateRoot } from '../employeeForm/employeeForm.styles';

const FORM_ID = 'employee-edit-form';

export default function EmployeeEditForm({ id }: { id: number }) {
  const { data, isLoading, isError, error, refetch } = useGetEmployeeQuery(id);

  if (isLoading) return <LoadingScreen />;
  if (isError) {
    return <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />;
  }
  if (!data) return null;

  return <EmployeeEditFormBody id={id} detail={data} />;
}

function EmployeeEditFormBody({ id, detail }: { id: number; detail: EmployeeDetail }) {
  const form = useEmployeeEditForm(id, detail);

  return (
    <>
      <PageHeaderActions>
        <CancelHeaderButton
          type="button"
          variant="outlined"
          onClick={form.handleCancel}
          disabled={form.isSaving}
        >
          취소
        </CancelHeaderButton>
        <PermissionGate menuCode={MENU_CODE.EMPLOYEES} action="write">
          <PrimaryPageHeaderButton
            type="submit"
            form={FORM_ID}
            disabled={form.isSaving}
            startIcon={
              form.isSaving ? <CircularProgress size={14} color="inherit" /> : <SaveIcon />
            }
          >
            저장
          </PrimaryPageHeaderButton>
        </PermissionGate>
      </PageHeaderActions>

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <EmployeeFormHero name={form.values.name} mode="edit" />
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
