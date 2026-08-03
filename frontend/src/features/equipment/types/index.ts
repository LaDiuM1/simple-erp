import type { FilterOption } from '@/shared/ui/GenericList';

/**
 * 백엔드 io.github.ladium1.erp.equipment 의 enum / DTO 미러.
 * BE 변경 시 함께 갱신.
 */

export const OUTPUT_UNIT = {
  KW: 'KW',
  TON: 'TON',
} as const;

export type OutputUnit = (typeof OUTPUT_UNIT)[keyof typeof OUTPUT_UNIT];

export const OUTPUT_UNIT_LABELS: Record<OutputUnit, string> = {
  KW: 'kW',
  TON: 'ton',
};

export const WARRANTY_FILTER_OPTIONS: FilterOption[] = [
  { value: 'ACTIVE', label: '보증중' },
  { value: 'EXPIRING', label: '만료 임박 (90일)' },
  { value: 'EXPIRED', label: '만료' },
];

/** 출력 값 + 단위 표시 조합 (예: "12 kW"). 값 없으면 null. */
export function formatOutput(value: number | null, unit: OutputUnit | null): string | null {
  if (value == null) return null;
  return `${value}${unit ? ` ${OUTPUT_UNIT_LABELS[unit]}` : ''}`;
}

export type WarrantyStatus = 'active' | 'expiring' | 'expired';

/** 만료일 → 보증 상태 (목록 / 상세의 텍스트 색 차등용). 만료일 미입력은 null. */
export function warrantyStatusOf(endDate: string | null): WarrantyStatus | null {
  if (!endDate) return null;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const end = new Date(`${endDate}T00:00:00`);
  if (end < today) return 'expired';
  const limit = new Date(today);
  limit.setDate(limit.getDate() + 90);
  return end <= limit ? 'expiring' : 'active';
}

export interface EquipmentSummary {
  id: number;
  customerId: number;
  customerName: string | null;
  contractId: number | null;
  contractNo: string | null;
  supplierId: number;
  supplierName: string | null;
  productId: number;
  productModelName: string | null;
  categoryName: string | null;
  outputValue: number | null;
  outputUnit: OutputUnit | null;
  serialNo: string | null;
  installAddress: string | null;
  installedDate: string | null;
  oscillatorWarrantyEndDate: string | null;
  generalWarrantyEndDate: string | null;
  warrantyInsurance: boolean;
}

export interface EquipmentReference {
  id: number;
  customerId: number;
  productModelName: string | null;
  serialNo: string | null;
  installAddress: string | null;
  installedDate: string | null;
  oscillatorWarrantyEndDate: string | null;
  generalWarrantyEndDate: string | null;
}

export interface EquipmentDetail {
  id: number;
  customerId: number;
  customerName: string | null;
  contractId: number | null;
  contractNo: string | null;
  supplierId: number;
  supplierName: string | null;
  productId: number;
  productModelName: string | null;
  categoryName: string | null;
  outputValue: number | null;
  outputUnit: OutputUnit | null;
  serialNo: string | null;
  installAddress: string | null;
  installedDate: string | null;
  confirmedDate: string | null;
  warrantyStartDate: string | null;
  oscillatorWarrantyMonths: number | null;
  generalWarrantyMonths: number | null;
  oscillatorWarrantyEndDate: string | null;
  generalWarrantyEndDate: string | null;
  warrantyInsurance: boolean;
  note: string | null;
}

export interface EquipmentCreateRequest {
  customerId: number;
  productId: number;
  outputValue: number | null;
  outputUnit: OutputUnit | null;
  serialNo: string | null;
  installAddress: string | null;
  installedDate: string | null;
  confirmedDate: string | null;
  warrantyStartDate: string | null;
  oscillatorWarrantyMonths: number | null;
  generalWarrantyMonths: number | null;
  warrantyInsurance: boolean;
  note: string | null;
}

export type EquipmentUpdateRequest = EquipmentCreateRequest;

export interface EquipmentSearchParams {
  customerId?: number | null;
  supplierId?: number | null;
  serialKeyword?: string | null;
  addressKeyword?: string | null;
  warranty?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

export interface EquipmentReferenceSearchParams {
  customerId: number;
  serialKeyword?: string | null;
  addressKeyword?: string | null;
  warranty?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type EquipmentListFilters = Omit<EquipmentSearchParams, 'page' | 'size' | 'sort'>;

export type EquipmentReferenceListFilters = Omit<
  EquipmentReferenceSearchParams,
  'page' | 'size' | 'sort'
>;

export interface EquipmentFormValues {
  /** 빈 문자열 = 미선택 */
  customerId: string;
  customerName: string;
  /** 빈 문자열 = 미선택 */
  productId: string;
  productModelName: string;
  outputValue: string;
  /** '' | 'KW' | 'TON' */
  outputUnit: string;
  serialNo: string;
  installAddress: string;
  installedDate: string;
  confirmedDate: string;
  warrantyStartDate: string;
  oscillatorWarrantyMonths: string;
  generalWarrantyMonths: string;
  /** 'true' | 'false' */
  warrantyInsurance: string;
  note: string;
}

export const EMPTY_EQUIPMENT_FORM: EquipmentFormValues = {
  customerId: '',
  customerName: '',
  productId: '',
  productModelName: '',
  outputValue: '',
  outputUnit: '',
  serialNo: '',
  installAddress: '',
  installedDate: '',
  confirmedDate: '',
  warrantyStartDate: '',
  oscillatorWarrantyMonths: '',
  generalWarrantyMonths: '',
  warrantyInsurance: 'false',
  note: '',
};

export function equipmentDetailToFormValues(d: EquipmentDetail): EquipmentFormValues {
  return {
    customerId: String(d.customerId),
    customerName: d.customerName ?? '',
    productId: String(d.productId),
    productModelName: d.productModelName ?? '',
    outputValue: d.outputValue == null ? '' : String(d.outputValue),
    outputUnit: d.outputUnit ?? '',
    serialNo: d.serialNo ?? '',
    installAddress: d.installAddress ?? '',
    installedDate: d.installedDate ?? '',
    confirmedDate: d.confirmedDate ?? '',
    warrantyStartDate: d.warrantyStartDate ?? '',
    oscillatorWarrantyMonths: d.oscillatorWarrantyMonths == null ? '' : String(d.oscillatorWarrantyMonths),
    generalWarrantyMonths: d.generalWarrantyMonths == null ? '' : String(d.generalWarrantyMonths),
    warrantyInsurance: String(d.warrantyInsurance),
    note: d.note ?? '',
  };
}

export function equipmentFormToRequest(v: EquipmentFormValues): EquipmentCreateRequest {
  return {
    customerId: Number(v.customerId),
    productId: Number(v.productId),
    outputValue: v.outputValue.trim() === '' ? null : Number(v.outputValue),
    outputUnit: v.outputUnit === '' ? null : (v.outputUnit as OutputUnit),
    serialNo: emptyToNull(v.serialNo),
    installAddress: emptyToNull(v.installAddress),
    installedDate: emptyToNull(v.installedDate),
    confirmedDate: emptyToNull(v.confirmedDate),
    warrantyStartDate: emptyToNull(v.warrantyStartDate),
    oscillatorWarrantyMonths: v.oscillatorWarrantyMonths.trim() === '' ? null : Number(v.oscillatorWarrantyMonths),
    generalWarrantyMonths: v.generalWarrantyMonths.trim() === '' ? null : Number(v.generalWarrantyMonths),
    warrantyInsurance: v.warrantyInsurance === 'true',
    note: emptyToNull(v.note),
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
