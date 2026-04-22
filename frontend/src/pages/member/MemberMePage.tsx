import Typography from '@mui/material/Typography';
import { useGetMyProfileQuery } from '@/features/member/api/memberApi';
import MemberProfileCard from '@/features/member/components/MemberProfileCard';
import LoadingSpinner from '@/shared/ui/LoadingSpinner';
import { PageRoot } from './MemberMePage.styles';

export default function MemberMePage() {
  const { data: profile, isLoading } = useGetMyProfileQuery();

  if (isLoading) return <LoadingSpinner />;
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
