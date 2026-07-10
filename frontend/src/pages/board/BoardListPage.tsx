import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { boardListColumns, boardListFilters } from '@/features/board/config/boardListConfig';
import { useBoardListPage } from '@/features/board/hooks/useBoardListPage';

export default function BoardListPage() {
  const { api, headerActions } = useBoardListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={boardListFilters} column={boardListColumns} />
    </>
  );
}
