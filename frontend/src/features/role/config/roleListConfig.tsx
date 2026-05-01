import LockRoundedIcon from '@mui/icons-material/LockRounded';
import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
} from '@/shared/ui/GenericList';
import { type RoleSummary } from '@/features/role/types';

export const roleListColumns: ColumnConfig<RoleSummary>[] = [
  {
    key: 'code',
    label: '권한 코드',
    sortable: true,
    sortDirection: 'asc',
    defaultSort: true,
    mobilePrimary: true,
    flex: 1,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary', display: 'inline-flex', alignItems: 'center', gap: '0.375rem' }}>
        {m.code}
        {m.system && (
          <LockRoundedIcon sx={{ fontSize: 14, color: 'text.disabled' }} titleAccess="시스템 권한" />
        )}
      </Typography>
    ),
  },
  { key: 'name', label: '권한명', sortable: true, sortDirection: 'asc', flex: 1.2 },
  { key: 'description', label: '설명', flex: 2.5 },
];

export const roleListFilters: FilterConfig[] = [
  { type: 'search', key: 'codeKeyword', placeholder: '권한 코드 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '권한명 검색' },
];
