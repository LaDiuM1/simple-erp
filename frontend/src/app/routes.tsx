import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from '@/shared/ui/layout/ProtectedRoute';
import LoginPage from '@/pages/login/LoginPage';
import DashboardPage from '@/pages/dashboard/DashboardPage';
import EmployeeMePage from '@/pages/employee/EmployeeMePage';
import EmployeeListPage from '@/pages/employee/EmployeeListPage';
import EmployeeCreatePage from '@/pages/employee/EmployeeCreatePage';
import EmployeeEditPage from '@/pages/employee/EmployeeEditPage';
import DepartmentListPage from '@/pages/department/DepartmentListPage';
import DepartmentCreatePage from '@/pages/department/DepartmentCreatePage';
import DepartmentEditPage from '@/pages/department/DepartmentEditPage';
import DepartmentHierarchyPage from '@/pages/department/DepartmentHierarchyPage';
import PositionListPage from '@/pages/position/PositionListPage';
import PositionCreatePage from '@/pages/position/PositionCreatePage';
import PositionEditPage from '@/pages/position/PositionEditPage';
import PositionRankingPage from '@/pages/position/PositionRankingPage';
import RoleListPage from '@/pages/role/RoleListPage';
import RoleCreatePage from '@/pages/role/RoleCreatePage';
import RoleEditPage from '@/pages/role/RoleEditPage';
import CodeRuleListPage from '@/pages/codeRule/CodeRuleListPage';
import CodeRuleEditPage from '@/pages/codeRule/CodeRuleEditPage';

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
        <Route path="/departments" element={<DepartmentListPage />} />
        <Route path="/departments/hierarchy" element={<DepartmentHierarchyPage />} />
        <Route path="/departments/new" element={<DepartmentCreatePage />} />
        <Route path="/departments/:id/edit" element={<DepartmentEditPage />} />
        <Route path="/positions" element={<PositionListPage />} />
        <Route path="/positions/ranking" element={<PositionRankingPage />} />
        <Route path="/positions/new" element={<PositionCreatePage />} />
        <Route path="/positions/:id/edit" element={<PositionEditPage />} />
        <Route path="/roles" element={<RoleListPage />} />
        <Route path="/roles/new" element={<RoleCreatePage />} />
        <Route path="/roles/:id/edit" element={<RoleEditPage />} />
        <Route path="/code-rules" element={<CodeRuleListPage />} />
        <Route path="/code-rules/:target/edit" element={<CodeRuleEditPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
