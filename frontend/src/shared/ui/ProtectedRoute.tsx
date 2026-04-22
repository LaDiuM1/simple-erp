import { Navigate } from 'react-router-dom';
import { useAppSelector } from '@/app/hooks';
import AppLayout from './AppLayout';

export default function ProtectedRoute() {
  const accessToken = useAppSelector((s) => s.auth.accessToken);
  if (!accessToken) return <Navigate to="/login" replace />;
  return <AppLayout />;
}
