import CircularProgress from '@mui/material/CircularProgress';
import AddIcon from '@mui/icons-material/Add';
import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import { CancelHeaderButton } from '@/shared/ui/GenericForm/GenericForm.styles';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { PrimaryPageHeaderButton } from '@/shared/ui/layout/PageHeaderButton';
import PermissionGate from '@/shared/ui/layout/PermissionGate';
import { useEmployeeCreateForm } from '@/features/employee/hooks/useEmployeeCreateForm';
import EmployeeFormHero from './EmployeeFormHero';
import AccountSection from './AccountSection';
import BasicInfoSection from './BasicInfoSection';
import AffiliationSection from './AffiliationSection';
import AddressSection from './AddressSection';
import { CreateForm, CreateRoot } from './EmployeeCreateForm.styles';

const FORM_ID = 'employee-create-form';

export default function EmployeeCreateForm() {
  const form = useEmployeeCreateForm();

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
              form.isSaving ? <CircularProgress size={14} color="inherit" /> : <AddIcon />
            }
          >
            등록
          </PrimaryPageHeaderButton>
        </PermissionGate>
      </PageHeaderActions>

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <EmployeeFormHero name={form.values.name} />
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
