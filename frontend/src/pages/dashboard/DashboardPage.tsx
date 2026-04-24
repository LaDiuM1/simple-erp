import { useGetMyProfileQuery } from '@/features/member/api/memberApi';
import {
  CardLabel,
  CardValue,
  DashboardCard,
  DashboardGrid,
  GreetingBlock,
  GreetingDate,
  GreetingTitle,
  PageRoot,
} from './DashboardPage.styles';

export default function DashboardPage() {
  const { data: profile } = useGetMyProfileQuery();

  const today = new Date().toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  });

  return (
    <PageRoot>
      <GreetingBlock>
        <GreetingTitle>안녕하세요, {profile?.name}님</GreetingTitle>
        <GreetingDate>{today}</GreetingDate>
      </GreetingBlock>

      <DashboardGrid>
        <InfoCard label="소속 부서" value={profile?.departmentName ?? '-'} />
        <InfoCard label="직위" value={profile?.positionName ?? '-'} />
        <InfoCard label="권한" value={profile?.roleName ?? '-'} />
        <InfoCard label="접근 가능 메뉴" value={`${profile?.menuPermissions.length ?? 0}개`} />
      </DashboardGrid>
    </PageRoot>
  );
}

function InfoCard({ label, value }: { label: string; value: string }) {
  return (
    <DashboardCard>
      <CardLabel>{label}</CardLabel>
      <CardValue>{value}</CardValue>
    </DashboardCard>
  );
}
