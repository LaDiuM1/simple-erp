import { lazy } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from '@/shared/ui/layout/ProtectedRoute';
import WritePermissionRoute from '@/shared/ui/layout/WritePermissionRoute';
import { MENU_CODE } from '@/shared/config/menuConfig';
import LoginPage from '@/pages/login/LoginPage';

const DashboardPage = lazy(() => import('@/pages/dashboard/DashboardPage'));
const EmployeeMePage = lazy(() => import('@/pages/employee/EmployeeMePage'));
const EmployeeListPage = lazy(() => import('@/pages/employee/EmployeeListPage'));
const EmployeeCreatePage = lazy(() => import('@/pages/employee/EmployeeCreatePage'));
const EmployeeEditPage = lazy(() => import('@/pages/employee/EmployeeEditPage'));
const EmployeeDetailPage = lazy(() => import('@/pages/employee/EmployeeDetailPage'));
const DepartmentListPage = lazy(() => import('@/pages/department/DepartmentListPage'));
const DepartmentCreatePage = lazy(() => import('@/pages/department/DepartmentCreatePage'));
const DepartmentEditPage = lazy(() => import('@/pages/department/DepartmentEditPage'));
const DepartmentDetailPage = lazy(() => import('@/pages/department/DepartmentDetailPage'));
const DepartmentHierarchyPage = lazy(() => import('@/pages/department/DepartmentHierarchyPage'));
const PositionListPage = lazy(() => import('@/pages/position/PositionListPage'));
const PositionCreatePage = lazy(() => import('@/pages/position/PositionCreatePage'));
const PositionEditPage = lazy(() => import('@/pages/position/PositionEditPage'));
const PositionDetailPage = lazy(() => import('@/pages/position/PositionDetailPage'));
const PositionRankingPage = lazy(() => import('@/pages/position/PositionRankingPage'));
const RoleListPage = lazy(() => import('@/pages/role/RoleListPage'));
const RoleCreatePage = lazy(() => import('@/pages/role/RoleCreatePage'));
const RoleEditPage = lazy(() => import('@/pages/role/RoleEditPage'));
const RoleDetailPage = lazy(() => import('@/pages/role/RoleDetailPage'));
const CodeRuleListPage = lazy(() => import('@/pages/codeRule/CodeRuleListPage'));
const CodeRuleDetailPage = lazy(() => import('@/pages/codeRule/CodeRuleDetailPage'));
const CodeRuleEditPage = lazy(() => import('@/pages/codeRule/CodeRuleEditPage'));
const CustomerListPage = lazy(() => import('@/pages/customer/CustomerListPage'));
const CustomerCreatePage = lazy(() => import('@/pages/customer/CustomerCreatePage'));
const CustomerEditPage = lazy(() => import('@/pages/customer/CustomerEditPage'));
const CustomerDetailPage = lazy(() => import('@/pages/customer/CustomerDetailPage'));
const SupplierListPage = lazy(() => import('@/pages/supplier/SupplierListPage'));
const SupplierCreatePage = lazy(() => import('@/pages/supplier/SupplierCreatePage'));
const SupplierEditPage = lazy(() => import('@/pages/supplier/SupplierEditPage'));
const SupplierDetailPage = lazy(() => import('@/pages/supplier/SupplierDetailPage'));
const ProductListPage = lazy(() => import('@/pages/product/ProductListPage'));
const ProductCategoryPage = lazy(() => import('@/pages/product/ProductCategoryPage'));
const ProductCreatePage = lazy(() => import('@/pages/product/ProductCreatePage'));
const ProductEditPage = lazy(() => import('@/pages/product/ProductEditPage'));
const ProductDetailPage = lazy(() => import('@/pages/product/ProductDetailPage'));
const SalesCustomerListPage = lazy(() => import('@/pages/salesCustomer/SalesCustomerListPage'));
const SalesCustomerDetailPage = lazy(() => import('@/pages/salesCustomer/SalesCustomerDetailPage'));
const ContractListPage = lazy(() => import('@/pages/contract/ContractListPage'));
const ContractCreatePage = lazy(() => import('@/pages/contract/ContractCreatePage'));
const ContractEditPage = lazy(() => import('@/pages/contract/ContractEditPage'));
const ContractDetailPage = lazy(() => import('@/pages/contract/ContractDetailPage'));
const EquipmentListPage = lazy(() => import('@/pages/equipment/EquipmentListPage'));
const EquipmentCreatePage = lazy(() => import('@/pages/equipment/EquipmentCreatePage'));
const EquipmentEditPage = lazy(() => import('@/pages/equipment/EquipmentEditPage'));
const EquipmentDetailPage = lazy(() => import('@/pages/equipment/EquipmentDetailPage'));
const AfterServiceListPage = lazy(() => import('@/pages/afterService/AfterServiceListPage'));
const AfterServiceCreatePage = lazy(() => import('@/pages/afterService/AfterServiceCreatePage'));
const AfterServiceEditPage = lazy(() => import('@/pages/afterService/AfterServiceEditPage'));
const AfterServiceDetailPage = lazy(() => import('@/pages/afterService/AfterServiceDetailPage'));
const EngineerPage = lazy(() => import('@/pages/afterService/EngineerPage'));
const SalesContactListPage = lazy(() => import('@/pages/salesContact/SalesContactListPage'));
const SalesContactCreatePage = lazy(() => import('@/pages/salesContact/SalesContactCreatePage'));
const SalesContactEditPage = lazy(() => import('@/pages/salesContact/SalesContactEditPage'));
const SalesContactDetailPage = lazy(() => import('@/pages/salesContact/SalesContactDetailPage'));
const ApprovalListPage = lazy(() => import('@/pages/approval/ApprovalListPage'));
const ApprovalCreatePage = lazy(() => import('@/pages/approval/ApprovalCreatePage'));
const ApprovalDetailPage = lazy(() => import('@/pages/approval/ApprovalDetailPage'));
const ExpenseListPage = lazy(() => import('@/pages/expense/ExpenseListPage'));
const ExpenseCreatePage = lazy(() => import('@/pages/expense/ExpenseCreatePage'));
const ExpenseDetailPage = lazy(() => import('@/pages/expense/ExpenseDetailPage'));
const AttendanceMePage = lazy(() => import('@/pages/attendance/AttendanceMePage'));
const AttendanceStatusPage = lazy(() => import('@/pages/attendance/AttendanceStatusPage'));
const LeaveMePage = lazy(() => import('@/pages/attendance/LeaveMePage'));
const LeaveCreatePage = lazy(() => import('@/pages/attendance/LeaveCreatePage'));
const LeaveStatusPage = lazy(() => import('@/pages/attendance/LeaveStatusPage'));
const LeaveBalancePage = lazy(() => import('@/pages/attendance/LeaveBalancePage'));
const BoardListPage = lazy(() => import('@/pages/board/BoardListPage'));
const BoardCreatePage = lazy(() => import('@/pages/board/BoardCreatePage'));
const BoardEditPage = lazy(() => import('@/pages/board/BoardEditPage'));
const BoardDetailPage = lazy(() => import('@/pages/board/BoardDetailPage'));
const DrivePage = lazy(() => import('@/pages/drive/DrivePage'));

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/employee/me" element={<EmployeeMePage />} />
        <Route path="/employees" element={<EmployeeListPage />} />
        <Route path="/employees/:id" element={<EmployeeDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.EMPLOYEES} />}>
          <Route path="/employees/new" element={<EmployeeCreatePage />} />
          <Route path="/employees/:id/edit" element={<EmployeeEditPage />} />
        </Route>
        <Route path="/departments" element={<DepartmentListPage />} />
        <Route path="/departments/hierarchy" element={<DepartmentHierarchyPage />} />
        <Route path="/departments/:id" element={<DepartmentDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.DEPARTMENTS} />}>
          <Route path="/departments/new" element={<DepartmentCreatePage />} />
          <Route path="/departments/:id/edit" element={<DepartmentEditPage />} />
        </Route>
        <Route path="/positions" element={<PositionListPage />} />
        <Route path="/positions/ranking" element={<PositionRankingPage />} />
        <Route path="/positions/:id" element={<PositionDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.POSITIONS} />}>
          <Route path="/positions/new" element={<PositionCreatePage />} />
          <Route path="/positions/:id/edit" element={<PositionEditPage />} />
        </Route>
        <Route path="/roles" element={<RoleListPage />} />
        <Route path="/roles/:id" element={<RoleDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.ROLES} />}>
          <Route path="/roles/new" element={<RoleCreatePage />} />
          <Route path="/roles/:id/edit" element={<RoleEditPage />} />
        </Route>
        <Route path="/code-rules" element={<CodeRuleListPage />} />
        <Route path="/code-rules/:target" element={<CodeRuleDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.CODE_RULES} />}>
          <Route path="/code-rules/:target/edit" element={<CodeRuleEditPage />} />
        </Route>
        <Route path="/customers" element={<CustomerListPage />} />
        <Route path="/customers/:id" element={<CustomerDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.CUSTOMERS} />}>
          <Route path="/customers/new" element={<CustomerCreatePage />} />
          <Route path="/customers/:id/edit" element={<CustomerEditPage />} />
        </Route>
        <Route path="/suppliers" element={<SupplierListPage />} />
        <Route path="/suppliers/:id" element={<SupplierDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.SUPPLIERS} />}>
          <Route path="/suppliers/new" element={<SupplierCreatePage />} />
          <Route path="/suppliers/:id/edit" element={<SupplierEditPage />} />
        </Route>
        <Route path="/products" element={<ProductListPage />} />
        <Route path="/products/categories" element={<ProductCategoryPage />} />
        <Route path="/products/:id" element={<ProductDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.PRODUCTS} />}>
          <Route path="/products/new" element={<ProductCreatePage />} />
          <Route path="/products/:id/edit" element={<ProductEditPage />} />
        </Route>
        <Route path="/sales-customers" element={<SalesCustomerListPage />} />
        <Route path="/sales-customers/:customerId" element={<SalesCustomerDetailPage />} />
        <Route path="/sales-contacts" element={<SalesContactListPage />} />
        <Route path="/sales-contacts/:id" element={<SalesContactDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.SALES_CONTACTS} />}>
          <Route path="/sales-contacts/new" element={<SalesContactCreatePage />} />
          <Route path="/sales-contacts/:id/edit" element={<SalesContactEditPage />} />
        </Route>
        <Route path="/contracts" element={<ContractListPage />} />
        <Route path="/contracts/:id" element={<ContractDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.CONTRACTS} />}>
          <Route path="/contracts/new" element={<ContractCreatePage />} />
          <Route path="/contracts/:id/edit" element={<ContractEditPage />} />
        </Route>
        <Route path="/equipments" element={<EquipmentListPage />} />
        <Route path="/equipments/:id" element={<EquipmentDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.EQUIPMENTS} />}>
          <Route path="/equipments/new" element={<EquipmentCreatePage />} />
          <Route path="/equipments/:id/edit" element={<EquipmentEditPage />} />
        </Route>
        <Route path="/after-services" element={<AfterServiceListPage />} />
        <Route path="/after-services/engineers" element={<EngineerPage />} />
        <Route path="/after-services/:id" element={<AfterServiceDetailPage />} />
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.AFTER_SERVICES} />}>
          <Route path="/after-services/new" element={<AfterServiceCreatePage />} />
          <Route path="/after-services/:id/edit" element={<AfterServiceEditPage />} />
        </Route>
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
