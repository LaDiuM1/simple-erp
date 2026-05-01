import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
} from '@/shared/ui/GenericList';
import { type DepartmentSummary } from '@/features/department/types';

export const departmentListColumns: ColumnConfig<DepartmentSummary>[] = [
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

export const departmentListFilters: FilterConfig[] = [
  { type: 'search', key: 'codeKeyword', placeholder: '부서 코드 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '부서명 검색' },
];
