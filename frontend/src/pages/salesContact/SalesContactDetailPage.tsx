import { useNavigate, useParams } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { usePermission } from '@/shared/hooks/usePermission';
import GenericHeaderDetails, {
  type HeaderDetailField,
} from '@/shared/ui/GenericHeaderDetails';
import { SideBySideGrid } from '@/shared/ui/layout/SideBySideGrid';
import { useGetSalesContactQuery } from '@/features/salesContact/api/salesContactApi';
import EmploymentList from '@/features/salesContact/components/EmploymentList';
import { DetailRoot } from '@/features/salesContact/components/salesContactDetail.styles';
import type { SalesContactDetail } from '@/features/salesContact/types';
import type { ApiError } from '@/shared/types/api';

export default function SalesContactDetailPage() {
  const { id } = useParams<{ id: string }>();
  const contactId = id ? Number(id) : undefined;
  if (!contactId || Number.isNaN(contactId)) return null;
  return <Body contactId={contactId} />;
}

function Body({ contactId }: { contactId: number }) {
  const navigate = useNavigate();
  const { canWrite } = usePermission(MENU_CODE.SALES_CONTACTS);
  const { data, isLoading, isError, error, refetch } = useGetSalesContactQuery(contactId);

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />;
  if (!data) return null;

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
        <SideBySideGrid>
          <EmploymentList contactId={contactId} employments={data.employments} />
        </SideBySideGrid>
      </DetailRoot>
    </>
  );
}

function contactInfoFields(d: SalesContactDetail): HeaderDetailField[] {
  return [
    { label: '이름', value: d.name },
    { label: '영문명', value: d.nameEn },
    { label: '휴대폰', value: d.mobilePhone },
    { label: '사무실 전화', value: d.officePhone },
    { label: '회사 이메일', value: d.email },
    { label: '개인 이메일', value: d.personalEmail },
    { label: '처음 만난 날', value: d.metAt },
    { label: '만난 경로', value: d.metVia },
    { label: '비고', value: d.note, fullWidth: true },
  ];
}
