import Typography from '@mui/material/Typography';
import {
  type ColumnConfig,
  type FilterConfig,
  type FilterOption,
} from '@/shared/ui/GenericList';
import Muted from '@/shared/ui/atoms/Muted';
import { formatKrw } from '@/shared/utils/formatKrw';
import CustomerSelectFilter from '@/features/customer/components/CustomerSelectFilter';
import { useGetEngineersQuery } from '@/features/afterService/api/afterServiceApi';
import ServiceStatusIndicator from '@/features/afterService/components/ServiceStatusIndicator';
import WarrantyDecisionText from '@/features/afterService/components/WarrantyDecisionText';
import {
  SERVICE_STATUS_OPTIONS,
  SERVICE_TYPE_LABELS,
  SERVICE_TYPE_OPTIONS,
  WARRANTY_DECISION_OPTIONS,
  equipmentLabelOf,
  type AfterServiceSummary,
  type Engineer,
} from '@/features/afterService/types';

/** 엔지니어 필터 옵션 — 소속 함께 노출. 비활성 포함 (과거 건 필터 목적). */
function mapEngineerOptions(data: unknown): FilterOption[] {
  return (data as Engineer[]).map((e) => ({
    value: e.id,
    label: e.affiliation ? `${e.name} (${e.affiliation})` : e.name,
  }));
}

export const afterServiceListColumns: ColumnConfig<AfterServiceSummary>[] = [
  {
    key: 'receiptNo',
    label: '접수번호',
    sortable: true,
    sortDirection: 'asc',
    mobilePrimary: true,
    width: 130,
    render: (m) => (
      <Typography sx={{ fontSize: '0.875rem', fontWeight: 600, color: 'text.primary' }}>
        {m.receiptNo}
      </Typography>
    ),
  },
  {
    key: 'customerName',
    label: '고객사',
    flex: 1.2,
    render: (m) => m.customerName ?? <Muted />,
  },
  {
    key: 'equipment',
    label: '설비',
    hideOnMobile: true,
    flex: 1.2,
    render: (m) =>
      m.equipmentId == null ? (
        <Muted />
      ) : (
        equipmentLabelOf(m.equipmentModelName, m.equipmentSerialNo)
      ),
  },
  {
    key: 'type',
    label: '유형',
    width: 90,
    render: (m) => SERVICE_TYPE_LABELS[m.type],
  },
  {
    key: 'receivedDate',
    label: '접수일',
    sortable: true,
    sortDirection: 'desc',
    defaultSort: true,
    width: 112,
  },
  {
    key: 'assignedEngineerName',
    label: '주 담당',
    hideOnMobile: true,
    width: 100,
    render: (m) => m.assignedEngineerName ?? <Muted />,
  },
  {
    key: 'warrantyDecision',
    label: '유상/무상',
    width: 90,
    render: (m) => <WarrantyDecisionText decision={m.warrantyDecision} />,
  },
  {
    key: 'billingAmount',
    label: '청구액',
    align: 'right',
    hideOnMobile: true,
    width: 120,
    render: (m) => (m.billingAmount == null ? <Muted /> : formatKrw(m.billingAmount)),
  },
  {
    key: 'expenseTotal',
    label: '경비 합계',
    align: 'right',
    hideOnMobile: true,
    width: 120,
    render: (m) => formatKrw(m.expenseTotal),
  },
  {
    key: 'status',
    label: '상태',
    sortable: true,
    sortDirection: 'asc',
    width: 90,
    render: (m) => <ServiceStatusIndicator status={m.status} />,
  },
];

export const afterServiceListFilters: FilterConfig[] = [
  { type: 'search', key: 'receiptNoKeyword', placeholder: '접수번호 검색' },
  {
    type: 'custom',
    key: 'customerId',
    render: ({ value, onChange }) => (
      <CustomerSelectFilter value={value} onChange={onChange} />
    ),
  },
  { type: 'select', key: 'type', label: '유형', options: SERVICE_TYPE_OPTIONS, minWidth: 110 },
  { type: 'select', key: 'status', label: '상태', options: SERVICE_STATUS_OPTIONS, minWidth: 100 },
  {
    type: 'select',
    key: 'warrantyDecision',
    label: '유상/무상',
    options: WARRANTY_DECISION_OPTIONS,
    minWidth: 110,
  },
  {
    type: 'select',
    key: 'engineerId',
    label: '주 담당',
    useOptions: useGetEngineersQuery,
    mapOptions: mapEngineerOptions,
    minWidth: 150,
  },
  { type: 'date', key: 'receivedDateFrom', label: '접수일 (부터)' },
  { type: 'date', key: 'receivedDateTo', label: '접수일 (까지)' },
];
