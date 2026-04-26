import { useNavigate, useParams } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import { MENU_PATH, MENU_CODE } from '@/shared/config/menuConfig';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { useGetSalesCustomerDetailQuery } from '@/features/salesCustomer/api/salesCustomerApi';
import ActivityList from '@/features/salesCustomer/components/ActivityList';
import AssignmentList from '@/features/salesCustomer/components/AssignmentList';
import {
  DetailRoot,
  HeaderCard,
  HeaderCustomerCode,
  HeaderCustomerName,
} from '@/features/salesCustomer/components/salesCustomerDetail.styles';
import { getErrorMessage } from '@/shared/api/error';

export default function SalesCustomerDetailPage() {
  const { customerId: param } = useParams<{ customerId: string }>();
  const customerId = param ? Number(param) : undefined;
  if (!customerId || Number.isNaN(customerId)) return null;
  return <Body customerId={customerId} />;
}

function Body({ customerId }: { customerId: number }) {
  const navigate = useNavigate();
  const { data, isLoading, isError, error, refetch } = useGetSalesCustomerDetailQuery(customerId);

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;
  if (!data) return null;

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
        <HeaderCard>
          <HeaderCustomerCode>{data.customerCode}</HeaderCustomerCode>
          <HeaderCustomerName>{data.customerName}</HeaderCustomerName>
        </HeaderCard>

        <ActivityList customerId={customerId} activities={data.activities} />
        <AssignmentList customerId={customerId} assignments={data.assignments} />
      </DetailRoot>
    </>
  );
}
