import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import ExpenseCreateForm from '@/features/expense/components/ExpenseCreateForm/ExpenseCreateForm';
import { useExpenseCreatePage } from '@/features/expense/hooks/useExpenseCreatePage';

export default function ExpenseCreatePage() {
  const { form, headerActions } = useExpenseCreatePage();

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <ExpenseCreateForm form={form} />
    </>
  );
}
