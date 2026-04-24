import { useNavigate } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import {
  mapIdName,
  type ColumnConfig,
  type FilterConfig,
  type ListApiConfig,
} from '@/shared/ui/GenericList';
import {
  useDeleteMemberMutation,
  useDownloadMembersExcel,
  useGetMembersQuery,
} from '@/features/member/api/memberApi';
import {
  useGetDepartmentsQuery,
  useGetPositionsQuery,
  useGetRolesQuery,
} from '@/features/reference/api/referenceApi';
import MemberStatusIndicator from '@/features/member/components/MemberStatusIndicator';
import {
  MEMBER_STATUS_OPTIONS,
  type MemberListFilters,
  type MemberSummary,
} from '@/features/member/types';

export const MEMBER_MENU_CODE = 'MDM_HRM';

export const memberColumn: ColumnConfig<MemberSummary>[] = [
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
  { key: 'status', label: '상태', sortable: true, sortDirection: 'asc', render: (m) => <MemberStatusIndicator status={m.status} /> },
];

export const memberSearchFilter: FilterConfig[] = [
  { type: 'search', key: 'keyword', placeholder: '이름 또는 로그인 ID 검색' },
  { type: 'select', key: 'departmentId', label: '부서', useOptions: useGetDepartmentsQuery, mapOptions: mapIdName },
  { type: 'select', key: 'positionId', label: '직책', useOptions: useGetPositionsQuery, mapOptions: mapIdName },
  { type: 'select', key: 'roleId', label: '권한', useOptions: useGetRolesQuery, mapOptions: mapIdName },
  { type: 'select', key: 'status', label: '상태', options: MEMBER_STATUS_OPTIONS, minWidth: 120 },
];

/**
 * 직원 목록용 ListApiConfig 를 생성하는 훅.
 * onEdit 이 navigate 를 사용하므로 컴포넌트 내부에서만 호출 가능 (module-level 상수 불가).
 */
export function useMemberListApi(): ListApiConfig<MemberSummary, MemberListFilters> {
  const navigate = useNavigate();
  return {
    menuCode: MEMBER_MENU_CODE,
    useList: useGetMembersQuery,
    useDelete: useDeleteMemberMutation,
    useExcel: useDownloadMembersExcel,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`/members/${m.id}/edit`),
  };
}
