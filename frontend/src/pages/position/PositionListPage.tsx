import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import {
  positionListColumns,
  positionListFilters,
} from '@/features/position/config/positionListConfig';
import { usePositionListPage } from '@/features/position/hooks/usePositionListPage';

export default function PositionListPage() {
  const { api, headerActions } = usePositionListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={positionListFilters} column={positionListColumns} />
    </>
  );
}
