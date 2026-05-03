import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetPositionsSummaryQuery } from '@/features/position/api/positionApi';
import {
  positionListColumns,
  positionListFilters,
} from '@/features/position/config/positionListConfig';
import type { PositionSummary } from '@/features/position/types';
import { useGetPositionsQuery } from '@/features/reference/api/referenceApi';

interface Props {
  label?: string;
  /** 직책 id (string). 빈 문자열 = 미선택. */
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
  /** 결과 목록에서 제외할 직책 id. */
  excludeId?: number;
}

const positionSelectConfig: EntitySelectConfig<PositionSummary> = {
  modalTitle: '직책 검색',
  searchAriaLabel: '직책 검색',
  useSearchList: useGetPositionsSummaryQuery,
  rowKey: (m) => m.id,
  rowLabel: (m) => m.name,
  searchFilter: positionListFilters,
  column: positionListColumns,
};

/** 직책 검색 SelectField — 표시 라벨은 reference 캐시 (`useGetPositionsQuery`) 로 lookup. */
export default function PositionSelectField({
  label = '직책',
  value,
  onChange,
  ...rest
}: Props) {
  const { data: lookup = [] } = useGetPositionsQuery();
  const valueLabel = lookup.find((p) => String(p.id) === value)?.name ?? '';

  return (
    <EntitySelectField
      {...rest}
      label={label}
      value={value}
      valueLabel={valueLabel}
      onChange={(id) => onChange(id)}
      config={positionSelectConfig}
    />
  );
}
