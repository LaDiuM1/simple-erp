import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetSalesContactsQuery } from '@/features/salesContact/api/salesContactApi';
import {
  customerScopedSalesContactSelectColumns,
  salesContactSelectColumns,
  salesContactSelectFilters,
} from '@/features/salesContact/config/salesContactListConfig';
import type {
  SalesContactListFilters,
  SalesContactSummary,
} from '@/features/salesContact/types';

interface Props {
  label?: string;
  /** 명부 id (string). 빈 문자열 = 미선택. */
  value: string;
  /** 표시 라벨 — 외부 보유. */
  valueLabel: string;
  onChange: (id: string, name: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
  /** 지정하면 해당 고객사에 현재 재직 중인 담당자만 조회한다. */
  customerId?: number | null;
}

const salesContactSelectConfig: EntitySelectConfig<
  SalesContactSummary,
  SalesContactListFilters
> = {
  modalTitle: '영업 명부 검색',
  searchAriaLabel: '명부 검색',
  useSearchList: useGetSalesContactsQuery,
  rowKey: (m) => m.id,
  rowLabel: (m) => m.name,
  searchFilter: salesContactSelectFilters,
  column: salesContactSelectColumns,
};

/** 영업 명부 검색 SelectField — EmployeeSelectField 와 동일 패턴 (외부 valueLabel). */
export default function SalesContactSelectField({ label = '영업 명부', customerId, ...rest }: Props) {
  const scoped = customerId != null;
  const config = scoped
    ? { ...salesContactSelectConfig, column: customerScopedSalesContactSelectColumns }
    : salesContactSelectConfig;

  return (
    <EntitySelectField
      {...rest}
      label={label}
      config={config}
      fixedQueryParams={scoped ? { customerId } : undefined}
      scopeKey={scoped ? `customer:${customerId}` : 'all-customers'}
    />
  );
}
