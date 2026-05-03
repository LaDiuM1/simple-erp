import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetRolesSummaryQuery } from '@/features/role/api/roleApi';
import {
  roleListColumns,
  roleListFilters,
} from '@/features/role/config/roleListConfig';
import type { RoleSummary } from '@/features/role/types';
import { useGetRolesQuery } from '@/features/reference/api/referenceApi';

interface Props {
  label?: string;
  /** 권한 id (string). 빈 문자열 = 미선택. */
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
  /** 결과 목록에서 제외할 권한 id. */
  excludeId?: number;
}

const roleSelectConfig: EntitySelectConfig<RoleSummary> = {
  modalTitle: '권한 검색',
  searchAriaLabel: '권한 검색',
  useSearchList: useGetRolesSummaryQuery,
  rowKey: (m) => m.id,
  rowLabel: (m) => m.name,
  searchFilter: roleListFilters,
  column: roleListColumns,
};

/** 권한 검색 SelectField — 표시 라벨은 reference 캐시 (`useGetRolesQuery`) 로 lookup. */
export default function RoleSelectField({
  label = '권한',
  value,
  onChange,
  ...rest
}: Props) {
  const { data: lookup = [] } = useGetRolesQuery();
  const valueLabel = lookup.find((r) => String(r.id) === value)?.name ?? '';

  return (
    <EntitySelectField
      {...rest}
      label={label}
      value={value}
      valueLabel={valueLabel}
      onChange={(id) => onChange(id)}
      config={roleSelectConfig}
    />
  );
}
