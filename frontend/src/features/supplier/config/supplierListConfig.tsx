import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
} from '@/shared/ui/GenericList';
import ActiveStatusIndicator from '@/shared/ui/atoms/ActiveStatusIndicator';
import Muted from '@/shared/ui/atoms/Muted';
import { ACTIVE_FILTER_OPTIONS, type SupplierSummary } from '@/features/supplier/types';

export const supplierListColumns: ColumnConfig<SupplierSummary>[] = [
  {
    key: 'name',
    label: '공급사명',
    sortable: true,
    sortDirection: 'asc',
    defaultSort: true,
    mobilePrimary: true,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.name}
      </Typography>
    ),
  },
  {
    key: 'nameKo',
    label: '한글 표기',
    sortable: true,
    sortDirection: 'asc',
    render: (m) => m.nameKo ?? <Muted />,
  },
  {
    key: 'country',
    label: '국가',
    hideOnMobile: true,
    render: (m) => m.country ?? <Muted />,
  },
  {
    key: 'active',
    label: '사용 여부',
    sortable: true,
    sortDirection: 'desc',
    width: 100,
    render: (m) => <ActiveStatusIndicator active={m.active} />,
  },
];

export const supplierListFilters: FilterConfig[] = [
  { type: 'search', key: 'keyword', placeholder: '공급사명 검색 (영문 / 한글)' },
  { type: 'select', key: 'active', label: '사용 여부', options: ACTIVE_FILTER_OPTIONS, minWidth: 120 },
];
