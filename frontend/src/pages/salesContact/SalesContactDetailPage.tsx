import { useNavigate, useParams } from 'react-router-dom';
import ArrowBackRoundedIcon from '@mui/icons-material/ArrowBackRounded';
import EditOutlinedIcon from '@mui/icons-material/EditOutlined';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import PageHeaderActions from '@/shared/ui/layout/PageHeaderActions';
import { usePermission } from '@/shared/hooks/usePermission';
import Muted from '@/shared/ui/atoms/Muted';
import { useGetSalesContactQuery } from '@/features/salesContact/api/salesContactApi';
import EmploymentList from '@/features/salesContact/components/EmploymentList';
import {
  DetailRoot,
  InfoGrid,
  InfoLabel,
  InfoSection,
  InfoTitle,
  InfoValue,
} from '@/features/salesContact/components/salesContactDetail.styles';
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
        <ContactInfo detail={data} />
        <EmploymentList contactId={contactId} employments={data.employments} />
      </DetailRoot>
    </>
  );
}

function ContactInfo({ detail }: { detail: SalesContactDetail }) {
  return (
    <InfoSection>
      <InfoTitle>명부 정보</InfoTitle>
      <InfoGrid>
        <InfoLabel>이름</InfoLabel>
        <InfoValue>{detail.name}</InfoValue>
        <InfoLabel>영문명</InfoLabel>
        <InfoValue>{detail.nameEn ?? <Muted />}</InfoValue>

        <InfoLabel>휴대폰</InfoLabel>
        <InfoValue>{detail.mobilePhone ?? <Muted />}</InfoValue>
        <InfoLabel>사무실 전화</InfoLabel>
        <InfoValue>{detail.officePhone ?? <Muted />}</InfoValue>

        <InfoLabel>회사 이메일</InfoLabel>
        <InfoValue>{detail.email ?? <Muted />}</InfoValue>
        <InfoLabel>개인 이메일</InfoLabel>
        <InfoValue>{detail.personalEmail ?? <Muted />}</InfoValue>

        <InfoLabel>처음 만난 날</InfoLabel>
        <InfoValue>{detail.metAt ?? <Muted />}</InfoValue>
        <InfoLabel>만난 경로</InfoLabel>
        <InfoValue>{detail.metVia ?? <Muted />}</InfoValue>

        <InfoLabel>비고</InfoLabel>
        <InfoValue style={{ gridColumn: '1 / -1' }}>{detail.note ?? <Muted />}</InfoValue>
      </InfoGrid>
    </InfoSection>
  );
}
