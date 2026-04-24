import { useGetMyProfileQuery } from '@/features/member/api/memberApi';
import MemberProfileCard from '@/features/member/components/MemberProfileCard';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import type { ApiError } from '@/shared/types/api';
import { PageRoot, PageTitle } from './MemberMePage.styles';

export default function MemberMePage() {
  const { data: profile, isLoading, isError, error, refetch } = useGetMyProfileQuery();

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />;
  if (!profile) return null;

  return (
    <PageRoot>
      <PageTitle>내 정보</PageTitle>
      <MemberProfileCard profile={profile} />
    </PageRoot>
  );
}
