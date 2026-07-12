import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useEquipmentCreateForm } from '@/features/equipment/hooks/useEquipmentCreateForm';
import BasicSection from '../equipmentForm/BasicSection';
import WarrantySection from '../equipmentForm/WarrantySection';
import { CreateForm, CreateRoot } from '../equipmentForm/equipmentForm.styles';

const FORM_ID = 'equipment-create-form';

export default function EquipmentCreateForm() {
  const form = useEquipmentCreateForm();

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'create',
            formId: FORM_ID,
            loading: form.isSaving,
            menuCode: MENU_CODE.EQUIPMENTS,
          },
        ]}
      />

      <CreateRoot>
        <CreateForm id={FORM_ID} onSubmit={form.handleSubmit} noValidate>
          <BasicSection form={form} />
          <WarrantySection form={form} />
        </CreateForm>
      </CreateRoot>

      <ConfirmModal
        isOpen={form.confirmOpen}
        title="설비 등록"
        message={`${form.values.customerName || '선택한 고객사'} 의 설비를 대장에 등록하시겠습니까?`}
        confirmLabel={form.isSaving ? '등록 중...' : '등록'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
