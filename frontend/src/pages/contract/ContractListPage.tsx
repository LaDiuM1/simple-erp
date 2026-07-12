import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  contractListColumns,
  contractListFilters,
} from '@/features/contract/config/contractListConfig';
import { useContractListPage } from '@/features/contract/hooks/useContractListPage';

export default function ContractListPage() {
  const { api, headerActions } = useContractListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={contractListFilters} column={contractListColumns} />
    </>
  );
}
