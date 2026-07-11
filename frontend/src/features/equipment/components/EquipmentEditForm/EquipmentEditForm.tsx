import { MENU_CODE } from '@/shared/config/menuConfig';
import ConfirmModal from '@/shared/ui/feedback/ConfirmModal';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useEquipmentEditForm } from '@/features/equipment/hooks/useEquipmentEditForm';
import type { EquipmentDetail } from '@/features/equipment/types';
import BasicSection from '../equipmentForm/BasicSection';
import WarrantySection from '../equipmentForm/WarrantySection';
import { CreateForm, CreateRoot } from '../equipmentForm/equipmentForm.styles';

const FORM_ID = 'equipment-edit-form';

/**
 * 설비 수정 폼 Body — outer (page) 가 detail 보장한 뒤 위임. form-state hook 의 invariant 충족.
 */
export default function EquipmentEditForm({ id, detail }: { id: number; detail: EquipmentDetail }) {
  const form = useEquipmentEditForm(id, detail);

  return (
    <>
      <PageHeaderActions
        actions={[
          { design: 'cancel', onClick: form.handleCancel, disabled: form.isSaving },
          {
            design: 'save',
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
        title="설비 수정"
        message={`${form.values.customerName || '선택한 고객사'} 설비의 정보를 저장하시겠습니까?`}
        confirmLabel={form.isSaving ? '저장 중...' : '저장'}
        onConfirm={form.handleConfirmedSubmit}
        onCancel={form.closeConfirm}
      />
    </>
  );
}
