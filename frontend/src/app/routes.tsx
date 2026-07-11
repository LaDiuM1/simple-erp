import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from '@/shared/ui/layout/ProtectedRoute';
import LoginPage from '@/pages/login/LoginPage';
import DashboardPage from '@/pages/dashboard/DashboardPage';
import EmployeeMePage from '@/pages/employee/EmployeeMePage';
import EmployeeListPage from '@/pages/employee/EmployeeListPage';
import EmployeeCreatePage from '@/pages/employee/EmployeeCreatePage';
import EmployeeEditPage from '@/pages/employee/EmployeeEditPage';
import EmployeeDetailPage from '@/pages/employee/EmployeeDetailPage';
import DepartmentListPage from '@/pages/department/DepartmentListPage';
import DepartmentCreatePage from '@/pages/department/DepartmentCreatePage';
import DepartmentEditPage from '@/pages/department/DepartmentEditPage';
import DepartmentDetailPage from '@/pages/department/DepartmentDetailPage';
import DepartmentHierarchyPage from '@/pages/department/DepartmentHierarchyPage';
import PositionListPage from '@/pages/position/PositionListPage';
import PositionCreatePage from '@/pages/position/PositionCreatePage';
import PositionEditPage from '@/pages/position/PositionEditPage';
import PositionDetailPage from '@/pages/position/PositionDetailPage';
import PositionRankingPage from '@/pages/position/PositionRankingPage';
import RoleListPage from '@/pages/role/RoleListPage';
import RoleCreatePage from '@/pages/role/RoleCreatePage';
import RoleEditPage from '@/pages/role/RoleEditPage';
import RoleDetailPage from '@/pages/role/RoleDetailPage';
import CodeRuleListPage from '@/pages/codeRule/CodeRuleListPage';
import CodeRuleDetailPage from '@/pages/codeRule/CodeRuleDetailPage';
import CodeRuleEditPage from '@/pages/codeRule/CodeRuleEditPage';
import CustomerListPage from '@/pages/customer/CustomerListPage';
import CustomerCreatePage from '@/pages/customer/CustomerCreatePage';
import CustomerEditPage from '@/pages/customer/CustomerEditPage';
import CustomerDetailPage from '@/pages/customer/CustomerDetailPage';
import SupplierListPage from '@/pages/supplier/SupplierListPage';
import SupplierCreatePage from '@/pages/supplier/SupplierCreatePage';
import SupplierEditPage from '@/pages/supplier/SupplierEditPage';
import SupplierDetailPage from '@/pages/supplier/SupplierDetailPage';
import ProductListPage from '@/pages/product/ProductListPage';
import ProductCategoryPage from '@/pages/product/ProductCategoryPage';
import ProductCreatePage from '@/pages/product/ProductCreatePage';
import ProductEditPage from '@/pages/product/ProductEditPage';
import ProductDetailPage from '@/pages/product/ProductDetailPage';
import SalesCustomerListPage from '@/pages/salesCustomer/SalesCustomerListPage';
import SalesCustomerDetailPage from '@/pages/salesCustomer/SalesCustomerDetailPage';
import SalesContactListPage from '@/pages/salesContact/SalesContactListPage';
import SalesContactCreatePage from '@/pages/salesContact/SalesContactCreatePage';
import SalesContactEditPage from '@/pages/salesContact/SalesContactEditPage';
import SalesContactDetailPage from '@/pages/salesContact/SalesContactDetailPage';
import ApprovalListPage from '@/pages/approval/ApprovalListPage';
import ApprovalCreatePage from '@/pages/approval/ApprovalCreatePage';
import ApprovalDetailPage from '@/pages/approval/ApprovalDetailPage';
import ExpenseListPage from '@/pages/expense/ExpenseListPage';
import ExpenseCreatePage from '@/pages/expense/ExpenseCreatePage';
import ExpenseDetailPage from '@/pages/expense/ExpenseDetailPage';
import AttendanceMePage from '@/pages/attendance/AttendanceMePage';
import AttendanceStatusPage from '@/pages/attendance/AttendanceStatusPage';
import LeaveMePage from '@/pages/attendance/LeaveMePage';
import LeaveCreatePage from '@/pages/attendance/LeaveCreatePage';
import LeaveStatusPage from '@/pages/attendance/LeaveStatusPage';
import LeaveBalancePage from '@/pages/attendance/LeaveBalancePage';
import BoardListPage from '@/pages/board/BoardListPage';
import BoardCreatePage from '@/pages/board/BoardCreatePage';
import BoardEditPage from '@/pages/board/BoardEditPage';
import BoardDetailPage from '@/pages/board/BoardDetailPage';
import DrivePage from '@/pages/drive/DrivePage';

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
        <Route path="/employees/:id" element={<EmployeeDetailPage />} />
        <Route path="/departments" element={<DepartmentListPage />} />
        <Route path="/departments/hierarchy" element={<DepartmentHierarchyPage />} />
        <Route path="/departments/new" element={<DepartmentCreatePage />} />
        <Route path="/departments/:id/edit" element={<DepartmentEditPage />} />
        <Route path="/departments/:id" element={<DepartmentDetailPage />} />
        <Route path="/positions" element={<PositionListPage />} />
        <Route path="/positions/ranking" element={<PositionRankingPage />} />
        <Route path="/positions/new" element={<PositionCreatePage />} />
        <Route path="/positions/:id/edit" element={<PositionEditPage />} />
        <Route path="/positions/:id" element={<PositionDetailPage />} />
        <Route path="/roles" element={<RoleListPage />} />
        <Route path="/roles/new" element={<RoleCreatePage />} />
        <Route path="/roles/:id/edit" element={<RoleEditPage />} />
        <Route path="/roles/:id" element={<RoleDetailPage />} />
        <Route path="/code-rules" element={<CodeRuleListPage />} />
        <Route path="/code-rules/:target/edit" element={<CodeRuleEditPage />} />
        <Route path="/code-rules/:target" element={<CodeRuleDetailPage />} />
        <Route path="/customers" element={<CustomerListPage />} />
        <Route path="/customers/new" element={<CustomerCreatePage />} />
        <Route path="/customers/:id/edit" element={<CustomerEditPage />} />
        <Route path="/customers/:id" element={<CustomerDetailPage />} />
        <Route path="/suppliers" element={<SupplierListPage />} />
        <Route path="/suppliers/new" element={<SupplierCreatePage />} />
        <Route path="/suppliers/:id/edit" element={<SupplierEditPage />} />
        <Route path="/suppliers/:id" element={<SupplierDetailPage />} />
        <Route path="/products" element={<ProductListPage />} />
        <Route path="/products/categories" element={<ProductCategoryPage />} />
        <Route path="/products/new" element={<ProductCreatePage />} />
        <Route path="/products/:id/edit" element={<ProductEditPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />
        <Route path="/sales-customers" element={<SalesCustomerListPage />} />
        <Route path="/sales-customers/:customerId" element={<SalesCustomerDetailPage />} />
        <Route path="/sales-contacts" element={<SalesContactListPage />} />
        <Route path="/sales-contacts/new" element={<SalesContactCreatePage />} />
        <Route path="/sales-contacts/:id/edit" element={<SalesContactEditPage />} />
        <Route path="/sales-contacts/:id" element={<SalesContactDetailPage />} />
        <Route path="/approvals" element={<ApprovalListPage />} />
        <Route path="/approvals/new" element={<ApprovalCreatePage />} />
        <Route path="/approvals/:id" element={<ApprovalDetailPage />} />
        <Route path="/expenses" element={<ExpenseListPage />} />
        <Route path="/expenses/new" element={<ExpenseCreatePage />} />
        <Route path="/expenses/:id" element={<ExpenseDetailPage />} />
        <Route path="/attendance" element={<AttendanceMePage />} />
        <Route path="/attendance/status" element={<AttendanceStatusPage />} />
        <Route path="/leaves" element={<LeaveMePage />} />
        <Route path="/leaves/new" element={<LeaveCreatePage />} />
        <Route path="/leaves/status" element={<LeaveStatusPage />} />
        <Route path="/leaves/balances" element={<LeaveBalancePage />} />
        <Route path="/boards" element={<BoardListPage />} />
        <Route path="/boards/new" element={<BoardCreatePage />} />
        <Route path="/boards/:id/edit" element={<BoardEditPage />} />
        <Route path="/boards/:id" element={<BoardDetailPage />} />
        <Route path="/drive" element={<DrivePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
