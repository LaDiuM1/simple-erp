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
import { useGetProductCategoriesQuery } from '@/features/product/api/productApi';
import type { ProductCategorySummary, ProductSummary } from '@/features/product/types';

/** 공급사 필터 옵션 — 영문 표기 (+ 한글 표기) 함께 노출. */
function mapSupplierOptions(data: unknown): FilterOption[] {
  return (data as SupplierInfo[]).map((s) => ({
    value: s.id,
    label: s.nameKo ? `${s.name} (${s.nameKo})` : s.name,
  }));
}

/** 카테고리 필터 옵션 — 관리 화면의 노출 순서 그대로. */
function mapCategoryOptions(data: unknown): FilterOption[] {
  return (data as ProductCategorySummary[]).map((c) => ({ value: c.id, label: c.name }));
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
    key: 'categoryName',
    label: '카테고리',
    width: 140,
    render: (m) => m.categoryName ?? <Muted />,
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

/**
 * 검색 모달용 컬럼 — 계약 폼의 제품 모델 SelectField 등 선택용이라 식별 정보만 노출.
 * 목록 페이지(`productListColumns`)와 의도적으로 분리 (customerSelectColumns 와 동일 정책).
 */
export const productSelectColumns: ColumnConfig<ProductSummary>[] = [
  {
    key: 'modelName',
    label: '모델명',
    sortable: true,
    sortDirection: 'asc',
    defaultSort: true,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.modelName}
      </Typography>
    ),
  },
  {
    key: 'categoryName',
    label: '카테고리',
    width: 140,
    render: (m) => m.categoryName ?? <Muted />,
  },
  {
    key: 'supplierName',
    label: '공급사',
    hideOnMobile: true,
    render: (m) => m.supplierName ?? <Muted />,
  },
  {
    key: 'active',
    label: '사용 여부',
    hideOnMobile: true,
    width: 100,
    render: (m) => <ActiveStatusIndicator active={m.active} />,
  },
];

export const productListFilters: FilterConfig[] = [
  { type: 'search', key: 'modelNameKeyword', placeholder: '모델명 검색' },
  {
    type: 'select',
    key: 'categoryId',
    label: '카테고리',
    useOptions: useGetProductCategoriesQuery,
    mapOptions: mapCategoryOptions,
    minWidth: 140,
  },
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
