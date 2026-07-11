import type { FilterOption } from '@/shared/ui/GenericList';

/**
 * 백엔드 io.github.ladium1.erp.contract 의 enum / DTO 미러.
 * BE 변경 시 함께 갱신.
 */

export const CONTRACT_STATUS = {
  CONTRACTED: 'CONTRACTED',
  ORDERED: 'ORDERED',
  ARRIVED: 'ARRIVED',
  INSTALLING: 'INSTALLING',
  INSTALLED: 'INSTALLED',
  SETTLED: 'SETTLED',
  CANCELED: 'CANCELED',
} as const;

export type ContractStatus = (typeof CONTRACT_STATUS)[keyof typeof CONTRACT_STATUS];

export const CONTRACT_STATUS_LABELS: Record<ContractStatus, string> = {
  CONTRACTED: '계약',
  ORDERED: '발주',
  ARRIVED: '입고',
  INSTALLING: '설치중',
  INSTALLED: '설치완료',
  SETTLED: '정산완료',
  CANCELED: '계약취소',
};

export const CONTRACT_STATUS_OPTIONS: FilterOption[] = (
  Object.keys(CONTRACT_STATUS_LABELS) as ContractStatus[]
).map((s) => ({ value: s, label: CONTRACT_STATUS_LABELS[s] }));

export const OUTPUT_UNIT = {
  KW: 'KW',
  TON: 'TON',
} as const;

export type OutputUnit = (typeof OUTPUT_UNIT)[keyof typeof OUTPUT_UNIT];

export const OUTPUT_UNIT_LABELS: Record<OutputUnit, string> = {
  KW: 'kW',
  TON: 'ton',
};

export const SUPPORT_PROGRAM_STATUS = {
  NONE: 'NONE',
  APPLIED: 'APPLIED',
  SELECTED: 'SELECTED',
  REJECTED: 'REJECTED',
} as const;

export type SupportProgramStatus =
  (typeof SUPPORT_PROGRAM_STATUS)[keyof typeof SUPPORT_PROGRAM_STATUS];

export const SUPPORT_PROGRAM_STATUS_LABELS: Record<SupportProgramStatus, string> = {
  NONE: '해당없음',
  APPLIED: '신청',
  SELECTED: '선정',
  REJECTED: '미선정',
};

/** 출력 값 + 단위 표시 조합 (예: "12 kW"). 값 없으면 null. */
export function formatOutput(value: number | null, unit: OutputUnit | null): string | null {
  if (value == null) return null;
  return `${value}${unit ? ` ${OUTPUT_UNIT_LABELS[unit]}` : ''}`;
}

export interface ContractSummary {
  id: number;
  contractNo: string;
  customerId: number;
  customerName: string | null;
  employeeId: number;
  employeeName: string | null;
  supplierId: number;
  supplierName: string | null;
  productId: number;
  productModelName: string | null;
  categoryName: string | null;
  outputValue: number | null;
  outputUnit: OutputUnit | null;
  finalAmount: number;
  outstandingAmount: number;
  contractDate: string;
  dueDate: string | null;
  supportProgramName: string | null;
  supportProgramStatus: SupportProgramStatus;
  status: ContractStatus;
}

export interface ContractPayment {
  id: number;
  label: string;
  plannedDate: string | null;
  plannedAmount: number | null;
  paidDate: string | null;
  paidAmount: number | null;
  invoiceDate: string | null;
  invoiceAmount: number | null;
  note: string | null;
}

export interface ContractNote {
  id: number;
  authorEmployeeId: number;
  authorName: string | null;
  content: string;
  createdAt: string;
}

export interface ContractDetail {
  id: number;
  contractNo: string;
  customerId: number;
  customerName: string | null;
  employeeId: number;
  employeeName: string | null;
  supplierId: number;
  supplierName: string | null;
  productId: number;
  productModelName: string | null;
  categoryName: string | null;
  outputValue: number | null;
  outputUnit: OutputUnit | null;
  optionText: string | null;
  initialAmount: number | null;
  finalAmount: number;
  paidTotal: number;
  outstandingAmount: number;
  cretopGrade: string | null;
  supportProgramName: string | null;
  supportProgramStatus: SupportProgramStatus;
  contractDate: string;
  dueDate: string | null;
  orderDate: string | null;
  expectedArrivalDate: string | null;
  arrivalDate: string | null;
  installedDate: string | null;
  settledDate: string | null;
  logisticsNote: string | null;
  status: ContractStatus;
  payments: ContractPayment[];
  notes: ContractNote[];
}

export interface ContractCreateRequest {
  /** 채번 규칙 AUTO 모드면 null — 시스템이 생성 */
  contractNo: string | null;
  customerId: number;
  employeeId: number;
  productId: number;
  outputValue: number | null;
  outputUnit: OutputUnit | null;
  optionText: string | null;
  initialAmount: number | null;
  finalAmount: number;
  cretopGrade: string | null;
  supportProgramName: string | null;
  supportProgramStatus: SupportProgramStatus;
  contractDate: string;
  dueDate: string | null;
  orderDate: string | null;
  expectedArrivalDate: string | null;
  arrivalDate: string | null;
  installedDate: string | null;
  settledDate: string | null;
  logisticsNote: string | null;
  status: ContractStatus;
}

/** 계약 번호는 발급 후 불변 — 수정 요청에 없음. */
export type ContractUpdateRequest = Omit<ContractCreateRequest, 'contractNo'>;

export interface ContractPaymentRequest {
  label: string;
  plannedDate: string | null;
  plannedAmount: number | null;
  paidDate: string | null;
  paidAmount: number | null;
  invoiceDate: string | null;
  invoiceAmount: number | null;
  note: string | null;
}

export interface ContractSearchParams {
  contractNoKeyword?: string | null;
  customerId?: number | null;
  employeeId?: number | null;
  supplierId?: number | null;
  status?: string | null;
  contractDateFrom?: string | null;
  contractDateTo?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type ContractListFilters = Omit<ContractSearchParams, 'page' | 'size' | 'sort'>;

export interface ContractFormValues {
  /** 채번 규칙 AUTO / 잠금 상태면 빈 문자열 (BE 자동 채번) */
  contractNo: string;
  /** 빈 문자열 = 미선택 */
  customerId: string;
  customerName: string;
  /** 계약자 (영업 담당). 빈 문자열 = 미선택 */
  employeeId: string;
  employeeName: string;
  /** 빈 문자열 = 미선택 */
  productId: string;
  productModelName: string;
  outputValue: string;
  /** '' | 'KW' | 'TON' */
  outputUnit: string;
  optionText: string;
  initialAmount: string;
  finalAmount: string;
  cretopGrade: string;
  supportProgramName: string;
  supportProgramStatus: string;
  contractDate: string;
  dueDate: string;
  orderDate: string;
  expectedArrivalDate: string;
  arrivalDate: string;
  installedDate: string;
  settledDate: string;
  logisticsNote: string;
  status: string;
}

export const EMPTY_CONTRACT_FORM: ContractFormValues = {
  contractNo: '',
  customerId: '',
  customerName: '',
  employeeId: '',
  employeeName: '',
  productId: '',
  productModelName: '',
  outputValue: '',
  outputUnit: '',
  optionText: '',
  initialAmount: '',
  finalAmount: '',
  cretopGrade: '',
  supportProgramName: '',
  supportProgramStatus: SUPPORT_PROGRAM_STATUS.NONE,
  contractDate: '',
  dueDate: '',
  orderDate: '',
  expectedArrivalDate: '',
  arrivalDate: '',
  installedDate: '',
  settledDate: '',
  logisticsNote: '',
  status: CONTRACT_STATUS.CONTRACTED,
};

export function contractDetailToFormValues(d: ContractDetail): ContractFormValues {
  return {
    contractNo: d.contractNo,
    customerId: String(d.customerId),
    customerName: d.customerName ?? '',
    employeeId: String(d.employeeId),
    employeeName: d.employeeName ?? '',
    productId: String(d.productId),
    productModelName: d.productModelName ?? '',
    outputValue: d.outputValue == null ? '' : String(d.outputValue),
    outputUnit: d.outputUnit ?? '',
    optionText: d.optionText ?? '',
    initialAmount: d.initialAmount == null ? '' : String(d.initialAmount),
    finalAmount: String(d.finalAmount),
    cretopGrade: d.cretopGrade ?? '',
    supportProgramName: d.supportProgramName ?? '',
    supportProgramStatus: d.supportProgramStatus,
    contractDate: d.contractDate,
    dueDate: d.dueDate ?? '',
    orderDate: d.orderDate ?? '',
    expectedArrivalDate: d.expectedArrivalDate ?? '',
    arrivalDate: d.arrivalDate ?? '',
    installedDate: d.installedDate ?? '',
    settledDate: d.settledDate ?? '',
    logisticsNote: d.logisticsNote ?? '',
    status: d.status,
  };
}

export function contractFormToCreateRequest(v: ContractFormValues): ContractCreateRequest {
  return {
    contractNo: v.contractNo.trim() === '' ? null : v.contractNo.trim(),
    ...contractFormToUpdateRequest(v),
  };
}

export function contractFormToUpdateRequest(v: ContractFormValues): ContractUpdateRequest {
  return {
    customerId: Number(v.customerId),
    employeeId: Number(v.employeeId),
    productId: Number(v.productId),
    outputValue: v.outputValue.trim() === '' ? null : Number(v.outputValue),
    outputUnit: v.outputUnit === '' ? null : (v.outputUnit as OutputUnit),
    optionText: emptyToNull(v.optionText),
    initialAmount: v.initialAmount.trim() === '' ? null : Number(v.initialAmount),
    finalAmount: Number(v.finalAmount),
    cretopGrade: emptyToNull(v.cretopGrade),
    supportProgramName: emptyToNull(v.supportProgramName),
    supportProgramStatus: v.supportProgramStatus as SupportProgramStatus,
    contractDate: v.contractDate,
    dueDate: emptyToNull(v.dueDate),
    orderDate: emptyToNull(v.orderDate),
    expectedArrivalDate: emptyToNull(v.expectedArrivalDate),
    arrivalDate: emptyToNull(v.arrivalDate),
    installedDate: emptyToNull(v.installedDate),
    settledDate: emptyToNull(v.settledDate),
    logisticsNote: emptyToNull(v.logisticsNote),
    status: v.status as ContractStatus,
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
