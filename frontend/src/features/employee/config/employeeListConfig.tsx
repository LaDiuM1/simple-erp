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
  type EmployeeReference,
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
  { key: 'birthDate', label: '생년월일', hideOnMobile: true, width: 130 },
  { key: 'status', label: '상태', sortable: true, sortDirection: 'asc', width: 100, render: (m) => <EmployeeStatusIndicator status={m.status} /> },
];

/**
 * 업무 참조 선택용 컬럼.
 * 직원 관리 목록과 달리 담당자·결재자 등을 식별하는 데 필요한 조직 정보만 노출한다.
 * 이메일·전화·입사일·생년월일·권한은 선택 판단과 무관하므로 참조 응답에서 제외한다.
 */
export const employeeSelectColumns: ColumnConfig<EmployeeReference>[] = [
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
  { key: 'departmentName', label: '부서', flex: 1.2 },
  { key: 'positionName', label: '직책', flex: 0.9 },
];

/** 과거 직원까지 선택하는 조회 필터에서는 재직 상태를 함께 보여준다. */
export const historicalEmployeeSelectColumns: ColumnConfig<EmployeeReference>[] = [
  ...employeeSelectColumns,
  {
    key: 'status',
    label: '상태',
    sortable: true,
    sortDirection: 'asc',
    width: 100,
    render: (m) => <EmployeeStatusIndicator status={m.status} />,
  },
];

const employeeStatusFilter: FilterConfig = {
  type: 'select',
  key: 'status',
  label: '상태',
  options: EMPLOYEE_STATUS_OPTIONS,
  minWidth: 120,
};

export const employeeListFilters: FilterConfig[] = [
  { type: 'search', key: 'loginIdKeyword', placeholder: '로그인 ID 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '이름 검색' },
  { type: 'select', key: 'departmentId', label: '부서', useOptions: useGetDepartmentsQuery, mapOptions: mapIdName },
  { type: 'select', key: 'positionId', label: '직책', useOptions: useGetPositionsQuery, mapOptions: mapIdName },
  { type: 'select', key: 'roleId', label: '권한', useOptions: useGetRolesQuery, mapOptions: mapIdName },
  employeeStatusFilter,
];

/** 직원 선택에 필요한 식별·조직 필터만 유지한다. */
export const employeeSelectFilters: FilterConfig[] = employeeListFilters.filter((filter) =>
  ['nameKeyword', 'departmentId', 'positionId'].includes(filter.key),
);

export const historicalEmployeeSelectFilters: FilterConfig[] = [
  ...employeeSelectFilters,
  employeeStatusFilter,
];
