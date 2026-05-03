import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetEmployeesQuery } from '@/features/employee/api/employeeApi';
import {
  employeeListColumns,
  employeeListFilters,
} from '@/features/employee/config/employeeListConfig';
import type { EmployeeSummary } from '@/features/employee/types';

interface Props {
  label?: string;
  /** 직원 id (string). 빈 문자열 = 미선택. */
  value: string;
  /** 표시 라벨 — 외부 보유 (form values 가 employeeId + employeeName 동시 관리). */
  valueLabel: string;
  onChange: (id: string, name: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
}

const employeeSelectConfig: EntitySelectConfig<EmployeeSummary> = {
  modalTitle: '직원 검색',
  searchAriaLabel: '직원 검색',
  useSearchList: useGetEmployeesQuery,
  rowKey: (m) => m.id,
  rowLabel: (m) => m.name,
  searchFilter: employeeListFilters,
  column: employeeListColumns,
};

/**
 * 직원 검색 SelectField — Department / Role 와 다르게 reference 캐시가 없어 (전체 목록 endpoint 미보유)
 * 표시 라벨을 외부 prop 으로 받음. 폼 values 가 employeeId + employeeName 둘 다 보유.
 */
export default function EmployeeSelectField({ label = '직원', ...rest }: Props) {
  return <EntitySelectField {...rest} label={label} config={employeeSelectConfig} />;
}
