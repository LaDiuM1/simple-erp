import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  afterServiceListColumns,
  afterServiceListFilters,
} from '@/features/afterService/config/afterServiceListConfig';
import { useAfterServiceListPage } from '@/features/afterService/hooks/useAfterServiceListPage';

export default function AfterServiceListPage() {
  const { api, headerActions } = useAfterServiceListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList
        api={api}
        searchFilter={afterServiceListFilters}
        column={afterServiceListColumns}
      />
    </>
  );
}
