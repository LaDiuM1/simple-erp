import { useParams } from 'react-router-dom';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import GenericHeaderDetails from '@/shared/ui/GenericHeaderDetails';
import GenericTabbedTable from '@/shared/ui/GenericTabbedTable';
import {
  contactInfoFields,
  useSalesContactDetailPage,
} from '@/features/salesContact/hooks/useSalesContactDetailPage';
import EmploymentTabModals from '@/features/salesContact/components/EmploymentTabModals/EmploymentTabModals';
import ActivityTabModals from '@/features/salesContact/components/ActivityTabModals/ActivityTabModals';
import { DetailRoot } from '@/features/salesContact/components/salesContactDetail.styles';

export default function SalesContactDetailPage() {
  const { id } = useParams<{ id: string }>();
  const contactId = Number(id);
  if (!contactId) return null;

  const { queries, headerActions, tabsList, tabs } = useSalesContactDetailPage(contactId);

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ detail }) => (
          <DetailRoot>
            <GenericHeaderDetails fields={contactInfoFields(detail)} />
            <GenericTabbedTable tabs={tabsList} />
          </DetailRoot>
        )}
      </QueryGate>
      <EmploymentTabModals modal={tabs.employment} />
      <ActivityTabModals modal={tabs.activity} />
    </>
  );
}
