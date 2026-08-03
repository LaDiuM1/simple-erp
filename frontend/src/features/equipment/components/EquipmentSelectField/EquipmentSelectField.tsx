import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetEquipmentReferencesQuery } from '@/features/equipment/api/equipmentApi';
import {
  equipmentReferenceFilters,
  equipmentSelectColumns,
} from '@/features/equipment/config/equipmentListConfig';
import type {
  EquipmentReference,
  EquipmentReferenceListFilters,
} from '@/features/equipment/types';

interface Props {
  label?: string;
  /** 설비 id (string). 빈 문자열 = 미선택. */
  value: string;
  /** 표시 라벨 — 외부 보유 (form values 가 equipmentId + equipmentLabel 동시 관리). */
  valueLabel: string;
  onChange: (id: string, name: string) => void;
  required?: boolean;
  helperText?: string;
  disabled?: boolean;
  placeholder?: string;
  /** 신규 선택 후보를 제한하는 고객사 id. */
  customerId: string | number;
}

const equipmentSelectConfig: EntitySelectConfig<
  EquipmentReference,
  EquipmentReferenceListFilters
> = {
  modalTitle: '설비 검색',
  searchAriaLabel: '설비 검색',
  useSearchList: useGetEquipmentReferencesQuery,
  rowKey: (m) => m.id,
  rowLabel: (m) => {
    const detail = m.serialNo
      ? ` (${m.serialNo})`
      : m.installAddress
        ? ` · ${m.installAddress}`
        : '';
    return `${m.productModelName ?? '모델 미상'}${detail}`;
  },
  searchFilter: equipmentReferenceFilters,
  column: equipmentSelectColumns,
};

/** AS 접수용 고객사 범위 설비 검색. */
export default function EquipmentSelectField({ label = '설비', customerId, ...rest }: Props) {
  return (
    <EntitySelectField
      {...rest}
      label={label}
      config={equipmentSelectConfig}
      fixedQueryParams={{ customerId: Number(customerId) }}
      scopeKey={`customer:${customerId}`}
    />
  );
}
