import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetContractEmployeeReferencesQuery } from '@/features/employee/api/employeeApi';
import {
  employeeSelectColumns,
  employeeSelectFilters,
  historicalEmployeeSelectColumns,
  historicalEmployeeSelectFilters,
} from '@/features/employee/config/employeeListConfig';
import type {
  EmployeeReference,
  EmployeeReferenceListFilters,
} from '@/features/employee/types';

interface Props {
  label?: string;
  value: string;
  valueLabel: string;
  onChange: (id: string, name: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
  dense?: boolean;
  activeOnly?: boolean;
}

const contractEmployeeSelectConfig: EntitySelectConfig<
  EmployeeReference,
  EmployeeReferenceListFilters
> = {
  modalTitle: '계약자 검색',
  searchAriaLabel: '계약자 검색',
  useSearchList: useGetContractEmployeeReferencesQuery,
  rowKey: (employee) => employee.id,
  rowLabel: (employee) => employee.name,
  searchFilter: employeeSelectFilters,
  column: employeeSelectColumns,
};

const historicalContractEmployeeSelectConfig: EntitySelectConfig<
  EmployeeReference,
  EmployeeReferenceListFilters
> = {
  ...contractEmployeeSelectConfig,
  searchFilter: historicalEmployeeSelectFilters,
  column: historicalEmployeeSelectColumns,
};

/** 현재 사용자의 계약 데이터 범위 안에서만 계약자를 선택한다. */
export default function ContractEmployeeSelectField({
  label = '계약자',
  activeOnly = true,
  ...rest
}: Props) {
  return (
    <EntitySelectField
      {...rest}
      label={label}
      config={activeOnly ? contractEmployeeSelectConfig : historicalContractEmployeeSelectConfig}
      fixedQueryParams={activeOnly ? { status: 'ACTIVE' } : undefined}
    />
  );
}
