import { useParams } from 'react-router-dom';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import GenericHeaderDetails from '@/shared/ui/GenericHeaderDetails';
import GenericTabbedTable from '@/shared/ui/GenericTabbedTable';
import {
  afterServiceInfoFields,
  useAfterServiceDetailPage,
} from '@/features/afterService/hooks/useAfterServiceDetailPage';
import VisitTabModals from '@/features/afterService/components/VisitTabModals/VisitTabModals';
import ExpenseTabModals from '@/features/afterService/components/ExpenseTabModals/ExpenseTabModals';
import { DetailRoot } from '@/features/afterService/components/afterServiceDetail.styles';

export default function AfterServiceDetailPage() {
  const { id } = useParams<{ id: string }>();
  const afterServiceId = Number(id);
  if (!afterServiceId) return null;

  const { queries, headerActions, tabsList, tabs } = useAfterServiceDetailPage(afterServiceId);

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ detail }) => (
          <DetailRoot>
            <GenericHeaderDetails fields={afterServiceInfoFields(detail)} />
            <GenericTabbedTable tabs={tabsList} />
          </DetailRoot>
        )}
      </QueryGate>
      <VisitTabModals modal={tabs.visit} />
      <ExpenseTabModals modal={tabs.expense} />
    </>
  );
}
