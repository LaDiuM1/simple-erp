import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
  type FilterOption,
} from '@/shared/ui/GenericList';
import ActiveStatusIndicator from '@/shared/ui/atoms/ActiveStatusIndicator';
import Muted from '@/shared/ui/atoms/Muted';
import { useGetSuppliersQuery } from '@/features/reference/api/referenceApi';
import type { SupplierInfo } from '@/features/reference/types';
import { ACTIVE_FILTER_OPTIONS } from '@/features/supplier/types';
import {
  PRODUCT_CATEGORY_LABELS,
  PRODUCT_CATEGORY_OPTIONS,
  type ProductSummary,
} from '@/features/product/types';

/** 공급사 필터 옵션 — 영문 표기 (+ 한글 표기) 함께 노출. */
function mapSupplierOptions(data: unknown): FilterOption[] {
  return (data as SupplierInfo[]).map((s) => ({
    value: s.id,
    label: s.nameKo ? `${s.name} (${s.nameKo})` : s.name,
  }));
}

export const productListColumns: ColumnConfig<ProductSummary>[] = [
  {
    key: 'modelName',
    label: '모델명',
    sortable: true,
    sortDirection: 'asc',
    defaultSort: true,
    mobilePrimary: true,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.modelName}
      </Typography>
    ),
  },
  {
    key: 'category',
    label: '카테고리',
    sortable: true,
    sortDirection: 'asc',
    width: 140,
    render: (m) => PRODUCT_CATEGORY_LABELS[m.category],
  },
  {
    key: 'supplierName',
    label: '공급사',
    render: (m) => m.supplierName ?? <Muted />,
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

export const productListFilters: FilterConfig[] = [
  { type: 'search', key: 'modelNameKeyword', placeholder: '모델명 검색' },
  { type: 'select', key: 'category', label: '카테고리', options: PRODUCT_CATEGORY_OPTIONS, minWidth: 140 },
  {
    type: 'select',
    key: 'supplierId',
    label: '공급사',
    useOptions: useGetSuppliersQuery,
    mapOptions: mapSupplierOptions,
    minWidth: 160,
  },
  { type: 'select', key: 'active', label: '사용 여부', options: ACTIVE_FILTER_OPTIONS, minWidth: 120 },
];
