import { useNavigate, useParams } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import Link from '@mui/material/Link';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { usePermission } from '@/shared/hooks/usePermission';
import { getErrorMessage } from '@/shared/api/error';
import GenericHeaderDetails, {
  type HeaderDetailField,
} from '@/shared/ui/GenericHeaderDetails';
import GenericTabbedTable from '@/shared/ui/GenericTabbedTable';
import { useGetSalesContactQuery } from '@/features/salesContact/api/salesContactApi';
import { useGetSalesActivitiesByContactQuery } from '@/features/salesCustomer/api/salesCustomerApi';
import { useSourceTab } from '@/features/salesContact/hooks/useSourceTab';
import { useEmploymentTab } from '@/features/salesContact/hooks/useEmploymentTab';
import { useActivityTab } from '@/features/salesContact/hooks/useActivityTab';
import { DetailRoot } from '@/features/salesContact/components/salesContactDetail.styles';
import type {
  SalesContactDetail,
} from '@/features/salesContact/types';
import type { SalesActivity } from '@/features/salesCustomer/types';

export default function SalesContactDetailPage() {
  const { id } = useParams<{ id: string }>();
  const contactId = id ? Number(id) : undefined;
  if (!contactId || Number.isNaN(contactId)) return null;
  return <Body contactId={contactId} />;
}

function Body({ contactId }: { contactId: number }) {
  const { canWrite } = usePermission(MENU_CODE.SALES_CONTACTS);
  const detailQuery = useGetSalesContactQuery(contactId);
  const activitiesQuery = useGetSalesActivitiesByContactQuery(contactId);

  if (detailQuery.isLoading || activitiesQuery.isLoading) return <LoadingScreen />;
  if (detailQuery.isError) {
    return (
      <ErrorScreen message={getErrorMessage(detailQuery.error)} onRetry={detailQuery.refetch} />
    );
  }
  if (activitiesQuery.isError) {
    return (
      <ErrorScreen message={getErrorMessage(activitiesQuery.error)} onRetry={activitiesQuery.refetch} />
    );
  }
  if (!detailQuery.data || !activitiesQuery.data) return null;

  return (
    <Content
      contactId={contactId}
      data={detailQuery.data}
      activities={activitiesQuery.data}
      canWrite={canWrite}
    />
  );
}

interface ContentProps {
  contactId: number;
  data: SalesContactDetail;
  activities: SalesActivity[];
  canWrite: boolean;
}

function Content({ contactId, data, activities, canWrite }: ContentProps) {
  const navigate = useNavigate();
  const sourceTab = useSourceTab(data.sources);
  const employmentTab = useEmploymentTab(contactId, data.employments);
  const activityTab = useActivityTab(activities);

  return (
    <>
      <PageHeaderActions
        actions={[
          {
            design: 'cancel',
            label: '목록으로',
            icon: <ArrowBackRoundedIcon sx={{ fontSize: 18 }} />,
            onClick: () => navigate(MENU_PATH[MENU_CODE.SALES_CONTACTS]),
          },
          ...(canWrite
            ? [
                {
                  design: 'create' as const,
                  label: '수정',
                  icon: <EditOutlinedIcon sx={{ fontSize: 18 }} />,
                  onClick: () => navigate(`${MENU_PATH[MENU_CODE.SALES_CONTACTS]}/${contactId}/edit`),
                  menuCode: MENU_CODE.SALES_CONTACTS,
                },
              ]
            : []),
        ]}
      />

      <DetailRoot>
        <GenericHeaderDetails fields={contactInfoFields(data)} />
        <GenericTabbedTable
          tabs={[employmentTab.tab, activityTab.tab, sourceTab.tab]}
        />
      </DetailRoot>

      {employmentTab.modals}
      {activityTab.modals}
    </>
  );
}

function contactInfoFields(d: SalesContactDetail): HeaderDetailField[] {
  return [
    { label: '이름', value: d.name },
    { label: '영문명', value: d.nameEn },
    {
      label: '휴대폰',
      value: d.mobilePhone ? (
        <Link 
          href={`tel:${d.mobilePhone}`} 
          underline="hover" 
          color="primary"
          sx={{ fontWeight: 500 }}
        >
          {d.mobilePhone}
        </Link>
      ) : (
        d.mobilePhone
      ),
    },
    {
      label: '전화번호',
      value: d.officePhone ? (
        <Link 
          href={`tel:${d.officePhone}`} 
          underline="hover" 
          color="primary"
          sx={{ fontWeight: 500 }}
        >
          {d.officePhone}
        </Link>
      ) : (
        d.officePhone
      ),
    },
    {
      label: '회사 이메일',
      value: d.email ? (
        <Link 
          href={`mailto:${d.email}`} 
          underline="hover" 
          color="primary"
          sx={{ fontWeight: 500 }}
        >
          {d.email}
        </Link>
      ) : (
        d.email
      ),
    },
    {
      label: '개인 이메일',
      value: d.personalEmail ? (
        <Link 
          href={`mailto:${d.personalEmail}`} 
          underline="hover" 
          color="primary"
          sx={{ fontWeight: 500 }}
        >
          {d.personalEmail}
        </Link>
      ) : (
        d.personalEmail
      ),
    },
    { label: '최초 미팅일', value: d.metAt },
    { label: '비고', value: d.note },
  ];
}
