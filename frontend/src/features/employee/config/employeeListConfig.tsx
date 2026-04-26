import { useNavigate } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import { MENU_CODE } from '@/shared/config/menuConfig';
import {
  mapIdName,
  type ColumnConfig,
  type FilterConfig,
  type ListApiConfig,
} from '@/shared/ui/GenericList';
import {
  useDeleteEmployeeMutation,
  useDownloadEmployeesExcel,
  useGetEmployeesQuery,
} from '@/features/employee/api/employeeApi';
import {
  useGetDepartmentsQuery,
  useGetPositionsQuery,
  useGetRolesQuery,
} from '@/features/reference/api/referenceApi';
import EmployeeStatusIndicator from '@/features/employee/components/EmployeeStatusIndicator';
import {
  EMPLOYEE_STATUS_OPTIONS,
  type EmployeeListFilters,
  type EmployeeSummary,
} from '@/features/employee/types';

export const employeeColumn: ColumnConfig<EmployeeSummary>[] = [
  {
    key: 'name',
    label: '이름',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.name}
      </Typography>
    ),
  },
  { key: 'loginId', label: '로그인 ID', sortable: true, sortDirection: 'asc' },
  { key: 'departmentName', label: '부서' },
  { key: 'positionName', label: '직책' },
  { key: 'roleName', label: '권한' },
  { key: 'email', label: '이메일', hideOnMobile: true },
  { key: 'phone', label: '연락처', hideOnMobile: true },
  { key: 'joinDate', label: '입사일', sortable: true, sortDirection: 'desc', defaultSort: true, hideOnMobile: true },
  { key: 'status', label: '상태', sortable: true, sortDirection: 'asc', render: (m) => <EmployeeStatusIndicator status={m.status} /> },
];

export const employeeSearchFilter: FilterConfig[] = [
  { type: 'search', key: 'loginIdKeyword', placeholder: '로그인 ID 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '이름 검색' },
  { type: 'select', key: 'departmentId', label: '부서', useOptions: useGetDepartmentsQuery, mapOptions: mapIdName },
  { type: 'select', key: 'positionId', label: '직책', useOptions: useGetPositionsQuery, mapOptions: mapIdName },
  { type: 'select', key: 'roleId', label: '권한', useOptions: useGetRolesQuery, mapOptions: mapIdName },
  { type: 'select', key: 'status', label: '상태', options: EMPLOYEE_STATUS_OPTIONS, minWidth: 120 },
];

/**
 * 직원 목록용 ListApiConfig 를 생성하는 훅.
 * onEdit 이 navigate 를 사용하므로 컴포넌트 내부에서만 호출 가능 (module-level 상수 불가).
 */
export function useEmployeeListApi(): ListApiConfig<EmployeeSummary, EmployeeListFilters> {
  const navigate = useNavigate();
  return {
    menuCode: MENU_CODE.EMPLOYEES,
    useList: useGetEmployeesQuery,
    useDelete: useDeleteEmployeeMutation,
    useExcel: useDownloadEmployeesExcel,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`/employees/${m.id}/edit`),
    checkBox: true,
  };
}
