import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetSalesContactsQuery } from '@/features/salesContact/api/salesContactApi';
import {
  salesContactListColumns,
  salesContactListFilters,
} from '@/features/salesContact/config/salesContactListConfig';
import type { SalesContactSummary } from '@/features/salesContact/types';

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
}

const salesContactSelectConfig: EntitySelectConfig<SalesContactSummary> = {
  modalTitle: '영업 명부 검색',
  searchAriaLabel: '명부 검색',
  useSearchList: useGetSalesContactsQuery,
  rowKey: (m) => m.id,
  rowLabel: (m) => m.name,
  searchFilter: salesContactListFilters,
  column: salesContactListColumns,
};

/** 영업 명부 검색 SelectField — EmployeeSelectField 와 동일 패턴 (외부 valueLabel). */
export default function SalesContactSelectField({ label = '영업 명부', ...rest }: Props) {
  return <EntitySelectField {...rest} label={label} config={salesContactSelectConfig} />;
}
