import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAppSelector } from '@/app/hooks';
import { useGetMyProfileQuery } from '@/features/employee/api/employeeApi';
import { getErrorMessage } from '@/shared/api/error';
import LoadingScreen from '@/shared/ui/feedback/LoadingScreen';
import ErrorScreen from '@/shared/ui/feedback/ErrorScreen';
import AppLayout from './AppLayout';

interface ProtectedRouteProps {
  environmentBanner?: ReactNode;
}

export default function ProtectedRoute({ environmentBanner }: ProtectedRouteProps) {
  const accessToken = useAppSelector((s) => s.auth.accessToken);
  const { isLoading, isError, error, refetch } = useGetMyProfileQuery(undefined, {
    skip: !accessToken,
    // 다른 세션에서 조직·권한이 변경된 경우 창에 다시 진입할 때 메뉴와 서버 권한을 맞춘다.
    refetchOnFocus: true,
  });

  if (!accessToken) return <Navigate to="/login" replace />;
  if (isLoading) return <LoadingScreen />;
  if (isError) return <ErrorScreen message={getErrorMessage(error)} onRetry={refetch} />;

  return <AppLayout environmentBanner={environmentBanner} />;
}
