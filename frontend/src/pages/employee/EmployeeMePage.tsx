import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';
import EmployeeProfileCard from '@/features/employee/components/EmployeeProfileCard';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import { getErrorMessage } from '@/shared/api/error';
import { PageRoot, PageTitle } from './EmployeeMePage.styles';

export default function EmployeeMePage() {
  const { data: profile, isLoading, isError, error, refetch } = useGetMyProfileQuery();

  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;
  if (!profile) return null;

  return (
    <PageRoot>
      <PageTitle>내 정보</PageTitle>
      <EmployeeProfileCard profile={profile} />
    </PageRoot>
  );
}
