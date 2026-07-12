import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  equipmentListColumns,
  equipmentListFilters,
} from '@/features/equipment/config/equipmentListConfig';
import { useEquipmentListPage } from '@/features/equipment/hooks/useEquipmentListPage';

export default function EquipmentListPage() {
  const { api, headerActions } = useEquipmentListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={equipmentListFilters} column={equipmentListColumns} />
    </>
  );
}
