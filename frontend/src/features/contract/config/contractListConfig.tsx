import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
  type FilterOption,
} from '@/shared/ui/GenericList';
import Muted from '@/shared/ui/atoms/Muted';
import { formatKrw } from '@/shared/utils/formatKrw';
import { useGetSuppliersQuery } from '@/features/reference/api/referenceApi';
import type { SupplierInfo } from '@/features/reference/types';
import ContractEmployeeSelectFilter from '@/features/contract/components/ContractEmployeeSelectFilter';
import CustomerSelectFilter from '@/features/customer/components/CustomerSelectFilter';
import ContractStatusIndicator from '@/features/contract/components/ContractStatusIndicator';
import {
  CONTRACT_STATUS_OPTIONS,
  formatOutput,
  type ContractSummary,
} from '@/features/contract/types';

/** 공급사 필터 옵션 — 영문 표기 (+ 한글 표기) 함께 노출 (제품 목록과 동일 규칙). */
function mapSupplierOptions(data: unknown): FilterOption[] {
  return (data as SupplierInfo[]).map((s) => ({
    value: s.id,
    label: s.nameKo ? `${s.name} (${s.nameKo})` : s.name,
  }));
}

export const contractListColumns: ColumnConfig<ContractSummary>[] = [
  {
    key: 'contractNo',
    label: '계약번호',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    width: 130,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.contractNo}
      </Typography>
    ),
  },
  {
    key: 'customerName',
    label: '고객사',
    flex: 1.3,
    render: (m) => m.customerName ?? <Muted />,
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
    label: '설비명',
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
    key: 'employeeName',
    label: '계약자',
    hideOnMobile: true,
    width: 90,
    render: (m) => m.employeeName ?? <Muted />,
  },
  {
    key: 'contractDate',
    label: '계약일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    width: 112,
  },
  {
    key: 'finalAmount',
    label: '계약금액',
    sortable: true,
    sortDirection: 'desc',
    align: 'right',
    hideOnMobile: true,
    width: 130,
    render: (m) => formatKrw(m.finalAmount),
  },
  {
    key: 'outstandingAmount',
    label: '미수금',
    align: 'right',
    width: 130,
    render: (m) => (
      <Typography
        component="span"
        sx={{
          fontSize: 'inherit',
          color: m.outstandingAmount > 0 ? 'warning.main' : 'text.disabled',
        }}
      >
        {formatKrw(m.outstandingAmount)}
      </Typography>
    ),
  },
  {
    key: 'status',
    label: '상태',
    sortable: true,
    sortDirection: 'asc',
    width: 96,
    render: (m) => <ContractStatusIndicator status={m.status} />,
  },
];

export const contractListFilters: FilterConfig[] = [
  { type: 'search', key: 'contractNoKeyword', placeholder: '계약번호 검색' },
  {
    type: 'custom',
    key: 'customerId',
    render: ({ value, onChange }) => (
      <CustomerSelectFilter value={value} onChange={onChange} />
    ),
  },
  {
    type: 'custom',
    key: 'employeeId',
    render: ({ value, onChange }) => (
      <ContractEmployeeSelectFilter value={value} onChange={onChange} />
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
  { type: 'select', key: 'status', label: '상태', options: CONTRACT_STATUS_OPTIONS, minWidth: 110 },
  { type: 'date', key: 'contractDateFrom', label: '계약 시작일' },
  { type: 'date', key: 'contractDateTo', label: '계약 종료일' },
];
