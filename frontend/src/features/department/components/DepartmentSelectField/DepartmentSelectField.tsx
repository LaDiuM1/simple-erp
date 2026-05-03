import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetDepartmentsSummaryQuery } from '@/features/department/api/departmentApi';
import {
  departmentListColumns,
  departmentListFilters,
} from '@/features/department/config/departmentListConfig';
import type { DepartmentSummary } from '@/features/department/types';
import { useGetDepartmentsQuery } from '@/features/reference/api/referenceApi';

interface Props {
  label?: string;
  /** 부서 id (string). 빈 문자열 = 미선택. */
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
  /** 결과 목록에서 제외할 부서 id (예: 자기 자신을 상위 부서로 못 고르도록). */
  excludeId?: number;
}

const departmentSelectConfig: EntitySelectConfig<DepartmentSummary> = {
  modalTitle: '부서 검색',
  searchAriaLabel: '부서 검색',
  useSearchList: useGetDepartmentsSummaryQuery,
  rowKey: (m) => m.id,
  rowLabel: (m) => m.name,
  searchFilter: departmentListFilters,
  column: departmentListColumns,
};

/**
 * 부서 검색 SelectField — 표시 라벨은 reference 캐시 (`useGetDepartmentsQuery`) 로 lookup.
 */
export default function DepartmentSelectField({
  label = '부서',
  value,
  onChange,
  ...rest
}: Props) {
  const { data: lookup = [] } = useGetDepartmentsQuery();
  const valueLabel = lookup.find((d) => String(d.id) === value)?.name ?? '';

  return (
    <EntitySelectField
      {...rest}
      label={label}
      value={value}
      valueLabel={valueLabel}
      onChange={(id) => onChange(id)}
      config={departmentSelectConfig}
    />
  );
}
