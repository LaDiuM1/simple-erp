import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from '@/shared/ui/layout/ProtectedRoute';
import LoginPage from '@/pages/login/LoginPage';
import DashboardPage from '@/pages/dashboard/DashboardPage';
import MemberMePage from '@/pages/member/MemberMePage';
import MemberListPage from '@/pages/member/MemberListPage';
import MemberCreatePage from '@/pages/member/MemberCreatePage';
import MemberEditPage from '@/pages/member/MemberEditPage';

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/member/me" element={<MemberMePage />} />
        <Route path="/members" element={<MemberListPage />} />
        <Route path="/members/new" element={<MemberCreatePage />} />
        <Route path="/members/:id/edit" element={<MemberEditPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
