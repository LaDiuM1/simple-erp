import { Navigate } from 'react-router-dom';
import { useAppSelector } from '@/app/hooks';
import { useGetMyProfileQuery } from '@/features/member/api/memberApi';
import LoadingSpinner from './LoadingSpinner';
import AppLayout from './AppLayout';

export default function ProtectedRoute() {
  const accessToken = useAppSelector((s) => s.auth.accessToken);
  const { isLoading } = useGetMyProfileQuery(undefined, { skip: !accessToken });

  if (!accessToken) return <Navigate to="/login" replace />;
  if (isLoading) return <LoadingSpinner />;

  return <AppLayout />;
}
