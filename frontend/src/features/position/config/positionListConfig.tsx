import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
} from '@/shared/ui/GenericList';
import { type PositionSummary } from '@/features/position/types';

export const positionListColumns: ColumnConfig<PositionSummary>[] = [
  {
    key: 'rankLevel',
    label: '서열',
    sortable: true,
    sortDirection: 'asc',
    defaultSort: true,
    width: 80,
    align: 'center',
  },
  {
    key: 'code',
    label: '직책 코드',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    flex: 1,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.code}
      </Typography>
    ),
  },
  { key: 'name', label: '직책명', sortable: true, sortDirection: 'asc', flex: 1.2 },
  { key: 'description', label: '설명', flex: 2.5 },
];

export const positionListFilters: FilterConfig[] = [
  { type: 'search', key: 'codeKeyword', placeholder: '직책 코드 검색' },
  { type: 'search', key: 'nameKeyword', placeholder: '직책명 검색' },
];
