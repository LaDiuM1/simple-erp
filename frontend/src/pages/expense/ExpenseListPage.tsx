import GenericList from '@/shared/ui/GenericList';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { expenseListColumns } from '@/features/expense/config/expenseListConfig';
import { useExpenseListPage } from '@/features/expense/hooks/useExpenseListPage';

export default function ExpenseListPage() {
  const { api, searchFilter, headerActions } = useExpenseListPage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <GenericList api={api} searchFilter={searchFilter} column={expenseListColumns} />
    </>
  );
}
