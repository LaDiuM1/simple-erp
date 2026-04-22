import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import { useGetMyProfileQuery } from '@/features/member/api/memberApi';
import { DashboardCard, DashboardGrid } from './DashboardPage.styles';

export default function DashboardPage() {
  const { data: profile } = useGetMyProfileQuery();

  const today = new Date().toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  });

  return (
    <Box sx={{ maxWidth: 900 }}>
      <Box sx={{ mb: '2rem' }}>
        <Typography sx={{ fontSize: '1.625rem', fontWeight: 700, color: 'text.primary', mb: '0.375rem' }}>
          안녕하세요, {profile?.name}님
        </Typography>
        <Typography sx={{ fontSize: '0.9375rem', color: 'text.secondary' }}>{today}</Typography>
      </Box>

      <DashboardGrid>
        <InfoCard label="소속 부서" value={profile?.departmentName ?? '-'} />
        <InfoCard label="직위" value={profile?.positionName ?? '-'} />
        <InfoCard label="권한" value={profile?.roleName ?? '-'} />
        <InfoCard label="접근 가능 메뉴" value={`${profile?.menuPermissions.length ?? 0}개`} />
      </DashboardGrid>
    </Box>
  );
}

function InfoCard({ label, value }: { label: string; value: string }) {
  return (
    <DashboardCard>
      <Typography
        sx={{ fontSize: '0.8125rem', fontWeight: 500, color: 'text.secondary', mb: '0.625rem', textTransform: 'uppercase', letterSpacing: '0.06em' }}
      >
        {label}
      </Typography>
      <Typography sx={{ fontSize: '1.25rem', fontWeight: 700, color: 'text.primary' }}>
        {value}
      </Typography>
    </DashboardCard>
  );
}
