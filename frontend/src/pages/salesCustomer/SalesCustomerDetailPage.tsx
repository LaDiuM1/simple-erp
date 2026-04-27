import { useNavigate, useParams } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import { MENU_PATH, MENU_CODE } from '@/shared/config/menuConfig';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import GenericHeaderDetails, {
  type HeaderDetailField,
} from '@/shared/ui/GenericHeaderDetails';
import { SideBySideGrid } from '@/shared/ui/layout/SideBySideGrid';
import { useGetSalesCustomerDetailQuery } from '@/features/salesCustomer/api/salesCustomerApi';
import ActivityList from '@/features/salesCustomer/components/ActivityList';
import AssignmentList from '@/features/salesCustomer/components/AssignmentList';
import CustomerEmploymentList from '@/features/salesCustomer/components/CustomerEmploymentList';
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
  const navigate = useNavigate();
  const salesQuery = useGetSalesCustomerDetailQuery(customerId);
  const customerQuery = useGetCustomerQuery(customerId);

  if (salesQuery.isLoading || customerQuery.isLoading) return <LoadingScreen />;
  if (salesQuery.isError) {
    return <ErrorScreen message={getErrorMessage(salesQuery.error)} onRetry={salesQuery.refetch} />;
  }
  if (customerQuery.isError) {
    return <ErrorScreen message={getErrorMessage(customerQuery.error)} onRetry={customerQuery.refetch} />;
  }
  if (!salesQuery.data || !customerQuery.data) return null;

  const sales = salesQuery.data;
  const customer = customerQuery.data;

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

        <SideBySideGrid>
          <ActivityList customerId={customerId} activities={sales.activities} />
          <AssignmentList customerId={customerId} assignments={sales.assignments} />
          <CustomerEmploymentList customerId={customerId} />
        </SideBySideGrid>
      </DetailRoot>
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
