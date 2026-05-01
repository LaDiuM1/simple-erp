import Typography from '@mui/material/Typography';
import {
  mapIdName,
  type ColumnConfig,
  type FilterConfig,
} from '@/shared/ui/GenericList';
import {
  useGetDepartmentsQuery,
  useGetPositionsQuery,
  useGetRolesQuery,
} from '@/features/reference/api/referenceApi';
import EmployeeStatusIndicator from '@/features/employee/components/EmployeeStatusIndicator';
import {
  EMPLOYEE_STATUS_OPTIONS,
  type EmployeeSummary,
} from '@/features/employee/types';

export const employeeListColumns: ColumnConfig<EmployeeSummary>[] = [
  {
    key: 'name',
    label: '이름',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    flex: 1,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.name}
      </Typography>
    ),
  },
  { key: 'loginId', label: '로그인 ID', sortable: true, sortDirection: 'asc', flex: 1.2 },
  { key: 'departmentName', label: '부서', flex: 1 },
  { key: 'positionName', label: '직책', flex: 0.8 },
  { key: 'roleName', label: '권한', flex: 0.8 },
  { key: 'email', label: '이메일', hideOnMobile: true, flex: 2.2 },
  { key: 'phone', label: '연락처', hideOnMobile: true, flex: 1.5 },
  { key: 'joinDate', label: '입사일', sortable: true, sortDirection: 'desc', defaultSort: true, hideOnMobile: true, width: 130 },
  { key: 'status', label: '상태', sortable: true, sortDirection: 'asc', width: 100, render: (m) => <EmployeeStatusIndicator status={m.status} /> },
];

export const employeeListFilters: FilterConfig[] = [
  { type: 'search', key: 'loginIdKeyword', placeholder: '로그인 ID 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '이름 검색' },
  { type: 'select', key: 'departmentId', label: '부서', useOptions: useGetDepartmentsQuery, mapOptions: mapIdName },
  { type: 'select', key: 'positionId', label: '직책', useOptions: useGetPositionsQuery, mapOptions: mapIdName },
  { type: 'select', key: 'roleId', label: '권한', useOptions: useGetRolesQuery, mapOptions: mapIdName },
  { type: 'select', key: 'status', label: '상태', options: EMPLOYEE_STATUS_OPTIONS, minWidth: 120 },
];
