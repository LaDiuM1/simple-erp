import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  salesContactListColumns,
  salesContactListFilters,
} from '@/features/salesContact/config/salesContactListConfig';
import { useSalesContactListPage } from '@/features/salesContact/hooks/useSalesContactListPage';
import AcquisitionSourceManageModal from '@/features/acquisitionSource/components/AcquisitionSourceManageModal';

export default function SalesContactListPage() {
  const { api, headerActions, manageModal } = useSalesContactListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={salesContactListFilters} column={salesContactListColumns} />
      <AcquisitionSourceManageModal open={manageModal.open} onClose={manageModal.onClose} />
    </>
  );
}
