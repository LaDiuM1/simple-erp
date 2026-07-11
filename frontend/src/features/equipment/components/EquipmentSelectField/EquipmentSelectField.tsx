import EntitySelectField, { type EntitySelectConfig } from '@/shared/ui/EntitySelectField';
import { useGetEquipmentsQuery } from '@/features/equipment/api/equipmentApi';
import {
  equipmentListFilters,
  equipmentSelectColumns,
} from '@/features/equipment/config/equipmentListConfig';
import type { EquipmentSummary } from '@/features/equipment/types';

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
}

const equipmentSelectConfig: EntitySelectConfig<EquipmentSummary> = {
  modalTitle: '설비 검색',
  searchAriaLabel: '설비 검색',
  useSearchList: useGetEquipmentsQuery,
  rowKey: (m) => m.id,
  rowLabel: (m) =>
    `${m.productModelName ?? '모델 미상'}${m.serialNo ? ` (${m.serialNo})` : ''}`,
  searchFilter: equipmentListFilters,
  column: equipmentSelectColumns,
};

/** 설비 대장 검색 SelectField — CustomerSelectField 와 동일 패턴 (외부 valueLabel). */
export default function EquipmentSelectField({ label = '설비', ...rest }: Props) {
  return <EntitySelectField {...rest} label={label} config={equipmentSelectConfig} />;
}
