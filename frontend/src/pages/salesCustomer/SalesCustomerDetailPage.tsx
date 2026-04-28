import { useNavigate, useParams } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import { MENU_PATH, MENU_CODE } from '@/shared/config/menuConfig';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import GenericHeaderDetails, {
  type HeaderDetailField,
} from '@/shared/ui/GenericHeaderDetails';
import GenericTabbedTable from '@/shared/ui/GenericTabbedTable';
import { useGetSalesCustomerDetailQuery } from '@/features/salesCustomer/api/salesCustomerApi';
import { useGetSalesContactEmploymentsByCustomerIdQuery } from '@/features/salesContact/api/salesContactApi';
import type { SalesCustomerDetail } from '@/features/salesCustomer/types';
import type { SalesContactEmployment } from '@/features/salesContact/types';
import { useActivityTab } from '@/features/salesCustomer/hooks/useActivityTab';
import { useAssignmentTab } from '@/features/salesCustomer/hooks/useAssignmentTab';
import { useCustomerEmploymentTab } from '@/features/salesContact/hooks/useCustomerEmploymentTab';
import { DetailRoot } from '@/features/salesCustomer/components/salesCustomerDetail.styles';
import { useGetCustomerQuery } from '@/features/customer/api/customerApi';
import {
  CUSTOMER_STATUS_LABELS,
  CUSTOMER_TYPE_LABELS,
  type CustomerDetail,
} from '@/features/customer/types';
import { getErrorMessage } from '@/shared/api/error';

export default function SalesCustomerDetailPage() {
  const { customerId: param } = useParams<{ customerId: string }>();
  const customerId = param ? Number(param) : undefined;
  if (!customerId || Number.isNaN(customerId)) return null;
  return <Body customerId={customerId} />;
}

function Body({ customerId }: { customerId: number }) {
  const salesQuery = useGetSalesCustomerDetailQuery(customerId);
  const customerQuery = useGetCustomerQuery(customerId);
  const employmentsQuery = useGetSalesContactEmploymentsByCustomerIdQuery(customerId);

  if (salesQuery.isLoading || customerQuery.isLoading || employmentsQuery.isLoading) {
    return <LoadingScreen />;
  }
  if (salesQuery.isError) {
    return <ErrorScreen message={getErrorMessage(salesQuery.error)} onRetry={salesQuery.refetch} />;
  }
  if (customerQuery.isError) {
    return <ErrorScreen message={getErrorMessage(customerQuery.error)} onRetry={customerQuery.refetch} />;
  }
  if (employmentsQuery.isError) {
    return (
      <ErrorScreen
        message={getErrorMessage(employmentsQuery.error)}
        onRetry={employmentsQuery.refetch}
      />
    );
  }
  if (!salesQuery.data || !customerQuery.data || !employmentsQuery.data) return null;

  return (
    <Content
      customerId={customerId}
      sales={salesQuery.data}
      customer={customerQuery.data}
      employments={employmentsQuery.data}
    />
  );
}

interface ContentProps {
  customerId: number;
  sales: SalesCustomerDetail;
  customer: CustomerDetail;
  employments: SalesContactEmployment[];
}

function Content({ customerId, sales, customer, employments }: ContentProps) {
  const navigate = useNavigate();
  const activityTab = useActivityTab(customerId, sales.activities);
  const assignmentTab = useAssignmentTab(customerId, sales.assignments);
  const customerEmploymentTab = useCustomerEmploymentTab(employments);

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'cancel',
            label: '목록으로',
            icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(MENU_PATH[MENU_CODE.SALES_CUSTOMERS]),
          },
        ]}
      />

      <DetailRoot>
        <GenericHeaderDetails fields={customerInfoFields(customer)} />
        <GenericTabbedTable
          tabs={[assignmentTab.tab, activityTab.tab, customerEmploymentTab.tab]}
        />
      </DetailRoot>

      {assignmentTab.modals}
      {activityTab.modals}
    </>
  );
}

function customerInfoFields(c: CustomerDetail): HeaderDetailField[] {
  const fullAddress = [c.zipCode ? `(${c.zipCode})` : null, c.roadAddress, c.detailAddress]
    .filter(Boolean)
    .join(' ');
  return [
    { label: '고객사 코드', value: c.code },
    { label: '고객사명', value: c.name },
    { label: '전화번호', value: c.phone },
    { label: '대표자', value: c.representative },
    { label: '유형', value: CUSTOMER_TYPE_LABELS[c.type] },
    { label: '상태', value: CUSTOMER_STATUS_LABELS[c.status] },
    { label: '주소', value: fullAddress || null, fullWidth: true },
  ];
}
