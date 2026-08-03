import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetCustomerReferencesQuery } from '@/features/customer/api/customerApi';
import {
  customerSelectColumns,
  customerSelectFilters,
} from '@/features/customer/config/customerListConfig';
import type { CustomerListFilters, CustomerReference } from '@/features/customer/types';

interface Props {
  label?: string;
  /** 고객사 id (string). 빈 문자열 = 미선택. */
  value: string;
  /** 표시 라벨 — 외부 보유 (form values 가 customerId + customerName 동시 관리). */
  valueLabel: string;
  onChange: (id: string, name: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
  /** 필터바 배치용 dense 톤 (height 36 / floating label 없음). */
  dense?: boolean;
}

const customerSelectConfig: EntitySelectConfig<CustomerReference, CustomerListFilters> = {
  modalTitle: '고객사 검색',
  searchAriaLabel: '고객사 검색',
  useSearchList: useGetCustomerReferencesQuery,
  rowKey: (m) => m.id,
  rowLabel: (m) => m.name,
  searchFilter: customerSelectFilters,
  column: customerSelectColumns,
};

/** 고객사 검색 SelectField — EmployeeSelectField 와 동일 패턴 (외부 valueLabel). */
export default function CustomerSelectField({ label = '고객사', ...rest }: Props) {
  return <EntitySelectField {...rest} label={label} config={customerSelectConfig} />;
}
