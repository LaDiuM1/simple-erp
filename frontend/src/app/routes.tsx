import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from '@/shared/ui/layout/ProtectedRoute';
import LoginPage from '@/pages/login/LoginPage';
import DashboardPage from '@/pages/dashboard/DashboardPage';
import EmployeeMePage from '@/pages/employee/EmployeeMePage';
import EmployeeListPage from '@/pages/employee/EmployeeListPage';
import EmployeeCreatePage from '@/pages/employee/EmployeeCreatePage';
import EmployeeEditPage from '@/pages/employee/EmployeeEditPage';

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/employee/me" element={<EmployeeMePage />} />
        <Route path="/employees" element={<EmployeeListPage />} />
        <Route path="/employees/new" element={<EmployeeCreatePage />} />
        <Route path="/employees/:id/edit" element={<EmployeeEditPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
