import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useContractCreateForm } from '@/features/contract/hooks/useContractCreateForm';
import BasicSection from '../contractForm/BasicSection';
import SpecAmountSection from '../contractForm/SpecAmountSection';
import SupportSection from '../contractForm/SupportSection';
import ScheduleSection from '../contractForm/ScheduleSection';
import { CreateForm, CreateRoot } from '../contractForm/contractForm.styles';

const FORM_ID = 'contract-create-form';

export default function ContractCreateForm() {
  const form = useContractCreateForm();

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'create',
            formId: FORM_ID,
            loading: form.isSaving,
            menuCode: MENU_CODE.CONTRACTS,
          },
        ]}
      />

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <BasicSection form={form} mode="create" />
          <SpecAmountSection form={form} />
          <SupportSection form={form} />
          <ScheduleSection form={form} />
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="계약 등록"
        message={`${form.values.customerName || '선택한 고객사'} 와(과) 의 계약을 등록하시겠습니까?`}
        confirmLabel={form.isSaving ? '등록 중...' : '등록'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
