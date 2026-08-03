import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
  type FilterOption,
} from '@/shared/ui/GenericList';
import Muted from '@/shared/ui/atoms/Muted';
import { useGetSuppliersQuery } from '@/features/reference/api/referenceApi';
import type { SupplierInfo } from '@/features/reference/types';
import CustomerSelectFilter from '@/features/customer/components/CustomerSelectFilter';
import WarrantyDateText from '@/features/equipment/components/WarrantyDateText';
import {
  WARRANTY_FILTER_OPTIONS,
  formatOutput,
  type EquipmentReference,
  type EquipmentSummary,
} from '@/features/equipment/types';

/** 공급사 필터 옵션 — 영문 표기 (+ 한글 표기) 함께 노출 (제품 목록과 동일 규칙). */
function mapSupplierOptions(data: unknown): FilterOption[] {
  return (data as SupplierInfo[]).map((s) => ({
    value: s.id,
    label: s.nameKo ? `${s.name} (${s.nameKo})` : s.name,
  }));
}

export const equipmentListColumns: ColumnConfig<EquipmentSummary>[] = [
  {
    key: 'customerName',
    label: '고객사',
    mobilePrimary: true,
    flex: 1.3,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.customerName ?? '-'}
      </Typography>
    ),
  },
  {
    key: 'categoryName',
    label: '유형',
    hideOnMobile: true,
    width: 110,
    render: (m) => m.categoryName ?? <Muted />,
  },
  {
    key: 'productModelName',
    label: '모델명',
    flex: 1,
    render: (m) => m.productModelName ?? <Muted />,
  },
  {
    key: 'output',
    label: '출력',
    hideOnMobile: true,
    width: 90,
    render: (m) => formatOutput(m.outputValue, m.outputUnit) ?? <Muted />,
  },
  {
    key: 'serialNo',
    label: '시리얼',
    sortable: true,
    sortDirection: 'asc',
    hideOnMobile: true,
    width: 120,
    render: (m) => m.serialNo ?? <Muted />,
  },
  {
    key: 'installAddress',
    label: '설치 주소',
    hideOnMobile: true,
    flex: 1.6,
    render: (m) => m.installAddress ?? <Muted />,
  },
  {
    key: 'installedDate',
    label: '설치일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    width: 112,
    render: (m) => m.installedDate ?? <Muted />,
  },
  {
    key: 'oscillatorWarrantyEndDate',
    label: '발진기 보증',
    sortable: true,
    sortDirection: 'asc',
    width: 130,
    render: (m) => <WarrantyDateText endDate={m.oscillatorWarrantyEndDate} />,
  },
  {
    key: 'generalWarrantyEndDate',
    label: '무상 AS',
    sortable: true,
    sortDirection: 'asc',
    width: 130,
    render: (m) => <WarrantyDateText endDate={m.generalWarrantyEndDate} />,
  },
];

/**
 * 검색 모달용 컬럼 — AS 접수 폼의 설비 SelectField 등 선택용이라 식별 정보만 노출.
 * 목록 페이지(`equipmentListColumns`)와 의도적으로 분리 (productSelectColumns 와 동일 정책).
 */
export const equipmentSelectColumns: ColumnConfig<EquipmentReference>[] = [
  {
    key: 'productModelName',
    label: '모델명',
    mobilePrimary: true,
    flex: 1,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.productModelName ?? '-'}
      </Typography>
    ),
  },
  {
    key: 'serialNo',
    label: '시리얼',
    width: 120,
    render: (m) => m.serialNo ?? <Muted />,
  },
  {
    key: 'installAddress',
    label: '설치 주소',
    flex: 1.5,
    render: (m) => m.installAddress ?? <Muted />,
  },
  {
    key: 'installedDate',
    label: '설치일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    hideOnMobile: true,
    width: 112,
    render: (m) => m.installedDate ?? <Muted />,
  },
  {
    key: 'generalWarrantyEndDate',
    label: '무상 AS',
    hideOnMobile: true,
    width: 152,
    render: (m) => <WarrantyDateText endDate={m.generalWarrantyEndDate} />,
  },
];

export const equipmentListFilters: FilterConfig[] = [
  {
    type: 'custom',
    key: 'customerId',
    render: ({ value, onChange }) => (
      <CustomerSelectFilter value={value} onChange={onChange} />
    ),
  },
  {
    type: 'select',
    key: 'supplierId',
    label: '공급사',
    useOptions: useGetSuppliersQuery,
    mapOptions: mapSupplierOptions,
    minWidth: 160,
  },
  { type: 'search', key: 'serialKeyword', placeholder: '시리얼 검색' },
  { type: 'search', key: 'addressKeyword', placeholder: '설치 주소 검색' },
  { type: 'select', key: 'warranty', label: '보증 상태', options: WARRANTY_FILTER_OPTIONS, minWidth: 150 },
];

export const equipmentReferenceFilters: FilterConfig[] = equipmentListFilters.filter(
  (filter) => ['serialKeyword', 'addressKeyword', 'warranty'].includes(filter.key),
);
