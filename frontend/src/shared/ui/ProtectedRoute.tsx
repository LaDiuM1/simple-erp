import { Navigate } from 'react-router-dom';
import { useAppSelector } from '@/app/hooks';
import { useGetMyProfileQuery } from '@/features/member/api/memberApi';
import type { ApiError } from '@/shared/types/api';
import LoadingScreen from './LoadingScreen';
import ErrorScreen from './ErrorScreen';
import AppLayout from './AppLayout';

export default function ProtectedRoute() {
  const accessToken = useAppSelector((s) => s.auth.accessToken);
  const { isLoading, isError, error, refetch } = useGetMyProfileQuery(undefined, {
    skip: !accessToken,
  });

  if (!accessToken) return <Navigate to="/login" replace />;
  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={(error as ApiError)?.message} onRetry={refetch} />;

  return <AppLayout />;
}
