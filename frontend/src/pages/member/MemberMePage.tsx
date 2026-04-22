import Typography from '@mui/material/Typography';
import { useGetMyProfileQuery } from '@/features/member/api/memberApi';
import MemberProfileCard from '@/features/member/components/MemberProfileCard';
import LoadingScreen from '@/shared/ui/LoadingScreen';
import ErrorScreen from '@/shared/ui/ErrorScreen';
import type { ApiError } from '@/shared/types/api';
import { PageRoot } from './MemberMePage.styles';

export default function MemberMePage() {
  const { data: profile, isLoading, isError, error, refetch } = useGetMyProfileQuery();

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />;
  if (!profile) return null;

  return (
    <PageRoot>
      <Typography sx={{ fontSize: '1.375rem', fontWeight: 700, color: 'text.primary', mb: '1.5rem' }}>
        내 정보
      </Typography>
      <MemberProfileCard profile={profile} />
    </PageRoot>
  );
}
