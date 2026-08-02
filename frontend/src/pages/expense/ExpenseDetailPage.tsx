import { useParams } from 'react-router-dom';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import GenericHeaderDetails from '@/shared/ui/GenericHeaderDetails';
import GenericTabbedTable from '@/shared/ui/GenericTabbedTable';
import {
  expenseInfoFields,
  useExpenseDetailPage,
} from '@/features/expense/hooks/useExpenseDetailPage';
import ExpenseItemTabModals from '@/features/expense/components/ExpenseItemTabModals/ExpenseItemTabModals';
import { DetailRoot } from '@/features/expense/components/expenseDetail.styles';

export default function ExpenseDetailPage() {
  const { id } = useParams<{ id: string }>();
  const expenseId = Number(id);
  return expenseId ? <ExpenseDetailContent expenseId={expenseId} /> : null;
}

function ExpenseDetailContent({ expenseId }: { expenseId: number }) {
  const { queries, headerActions, tabsList, tabs } = useExpenseDetailPage(expenseId);

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ detail }) => (
          <DetailRoot>
            <GenericHeaderDetails fields={expenseInfoFields(detail)} />
            <GenericTabbedTable tabs={tabsList} />
          </DetailRoot>
        )}
      </QueryGate>
      <ExpenseItemTabModals modal={tabs.item} />
    </>
  );
}
