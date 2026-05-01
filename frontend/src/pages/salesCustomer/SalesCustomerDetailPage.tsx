import { useParams } from 'react-router-dom';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import QueryGate from '@/shared/ui/feedback/QueryGate';
import GenericHeaderDetails from '@/shared/ui/GenericHeaderDetails';
import GenericTabbedTable from '@/shared/ui/GenericTabbedTable';
import {
  customerInfoFields,
  useSalesCustomerDetailPage,
} from '@/features/salesCustomer/hooks/useSalesCustomerDetailPage';
import AssignmentTabModals from '@/features/salesCustomer/components/AssignmentTabModals/AssignmentTabModals';
import CustomerActivityTabModals from '@/features/salesCustomer/components/CustomerActivityTabModals/CustomerActivityTabModals';
import { DetailRoot } from '@/features/salesCustomer/components/salesCustomerDetail.styles';

export default function SalesCustomerDetailPage() {
  const { customerId: param } = useParams<{ customerId: string }>();
  const customerId = Number(param);
  if (!customerId) return null;

  const { queries, headerActions, tabsList, tabs } = useSalesCustomerDetailPage(customerId);

  return (
    <>
      <PageHeaderActions actions={headerActions} />
      <QueryGate queries={queries}>
        {({ customer }) => (
          <DetailRoot>
            <GenericHeaderDetails fields={customerInfoFields(customer)} />
            <GenericTabbedTable tabs={tabsList} />
          </DetailRoot>
        )}
      </QueryGate>
      <AssignmentTabModals modal={tabs.assignment} />
      <CustomerActivityTabModals modal={tabs.activity} />
    </>
  );
}
