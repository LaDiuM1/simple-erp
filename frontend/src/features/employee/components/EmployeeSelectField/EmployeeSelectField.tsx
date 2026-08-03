import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetEmployeeReferencesQuery } from '@/features/employee/api/employeeApi';
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
  /** 직원 id (string). 빈 문자열 = 미선택. */
  value: string;
  /** 표시 라벨 — 외부 보유 (form values 가 employeeId + employeeName 동시 관리). */
  valueLabel: string;
  onChange: (id: string, name: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
  /** 필터바 배치용 dense 톤 (height 36 / floating label 없음). */
  dense?: boolean;
  /** 목록 조회처럼 과거 직원까지 찾아야 하는 문맥에서만 false. 신규 참조 선택은 기본 ACTIVE. */
  activeOnly?: boolean;
  /** 선택 결과에서 제외할 직원 id (예: 기안자 본인). */
  excludeId?: number;
  /** 선택 결과에서 제외할 복수 직원 id (예: 이미 활성 배정된 담당자). */
  excludeIds?: number[];
}

const employeeSelectConfig: EntitySelectConfig<EmployeeReference, EmployeeReferenceListFilters> = {
  modalTitle: '직원 검색',
  searchAriaLabel: '직원 검색',
  useSearchList: useGetEmployeeReferencesQuery,
  rowKey: (m) => m.id,
  rowLabel: (m) => m.name,
  searchFilter: historicalEmployeeSelectFilters,
  column: historicalEmployeeSelectColumns,
};

const activeEmployeeSelectConfig: EntitySelectConfig<
  EmployeeReference,
  EmployeeReferenceListFilters
> = {
  ...employeeSelectConfig,
  searchFilter: employeeSelectFilters,
  column: employeeSelectColumns,
};

/**
 * 직원 검색 SelectField — Department / Role 와 다르게 reference 캐시가 없어 (전체 목록 endpoint 미보유)
 * 표시 라벨을 외부 prop 으로 받음. 폼 values 가 employeeId + employeeName 둘 다 보유.
 */
export default function EmployeeSelectField({
  label = '직원',
  activeOnly = true,
  ...rest
}: Props) {
  return (
    <EntitySelectField
      {...rest}
      label={label}
      config={activeOnly ? activeEmployeeSelectConfig : employeeSelectConfig}
      fixedQueryParams={activeOnly ? { status: 'ACTIVE' } : undefined}
    />
  );
}
