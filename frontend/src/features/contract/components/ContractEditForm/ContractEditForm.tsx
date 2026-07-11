import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useContractEditForm } from '@/features/contract/hooks/useContractEditForm';
import type { ContractDetail } from '@/features/contract/types';
import BasicSection from '../contractForm/BasicSection';
import SpecAmountSection from '../contractForm/SpecAmountSection';
import SupportSection from '../contractForm/SupportSection';
import ScheduleSection from '../contractForm/ScheduleSection';
import { CreateForm, CreateRoot } from '../contractForm/contractForm.styles';

const FORM_ID = 'contract-edit-form';

/**
 * 계약 수정 폼 Body — outer (page) 가 detail 보장한 뒤 위임. form-state hook 의 invariant 충족.
 */
export default function ContractEditForm({ id, detail }: { id: number; detail: ContractDetail }) {
  const form = useContractEditForm(id, detail);

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'save',
            formId: FORM_ID,
            loading: form.isSaving,
            menuCode: MENU_CODE.CONTRACTS,
          },
        ]}
      />

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <BasicSection form={form} mode="edit" />
          <SpecAmountSection form={form} />
          <SupportSection form={form} />
          <ScheduleSection form={form} />
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="계약 수정"
        message={`${form.values.contractNo} 계약의 정보를 저장하시겠습니까?`}
        confirmLabel={form.isSaving ? '저장 중...' : '저장'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
