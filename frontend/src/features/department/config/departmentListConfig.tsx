import { useNavigate } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import { MENU_CODE, MENU_PATH } from '@/shared/config/menuConfig';
import {
  type ColumnConfig,
  type FilterConfig,
  type ListApiConfig,
} from '@/shared/ui/GenericList';
import {
  useDeleteDepartmentMutation,
  useGetDepartmentsSummaryQuery,
} from '@/features/department/api/departmentApi';
import {
  type DepartmentListFilters,
  type DepartmentSummary,
} from '@/features/department/types';

export const departmentColumn: ColumnConfig<DepartmentSummary>[] = [
  {
    key: 'code',
    label: '부서 코드',
    sortable: true,
    sortDirection: 'asc',
    defaultSort: true,
    mobilePrimary: true,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.code}
      </Typography>
    ),
  },
  { key: 'name', label: '부서명', sortable: true, sortDirection: 'asc' },
  { key: 'parentName', label: '상위 부서' },
];

export const departmentSearchFilter: FilterConfig[] = [
  { type: 'search', key: 'codeKeyword', placeholder: '부서 코드 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '부서명 검색' },
];

/**
 * 부서 목록용 ListApiConfig 를 생성하는 훅.
 * onEdit 이 navigate 를 사용하므로 컴포넌트 내부에서만 호출 가능.
 */
export function useDepartmentListApi(): ListApiConfig<DepartmentSummary, DepartmentListFilters> {
  const navigate = useNavigate();
  return {
    menuCode: MENU_CODE.DEPARTMENTS,
    useList: useGetDepartmentsSummaryQuery,
    useDelete: useDeleteDepartmentMutation,
    rowKey: (m) => m.id,
    onEdit: (m) => navigate(`${MENU_PATH[MENU_CODE.DEPARTMENTS]}/${m.id}/edit`),
  };
}
