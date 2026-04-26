import { Navigate } from 'react-router-dom';
import { useAppSelector } from '@/app/hooks';
import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';
import { getErrorMessage } from '@/shared/api/error';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import AppLayout from './AppLayout';

export default function ProtectedRoute() {
  const accessToken = useAppSelector((s) => s.auth.accessToken);
  const { isLoading, isError, error, refetch } = useGetMyProfileQuery(undefined, {
    skip: !accessToken,
  });

  if (!accessToken) return <Navigate to="/login" replace />;
  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;

  return <AppLayout />;
}
