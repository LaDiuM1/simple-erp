import type { FilterOption } from '@/shared/ui/GenericList';

/**
 * 백엔드 io.github.ladium1.erp.afterservice 의 enum / DTO 미러.
 * BE 변경 시 함께 갱신.
 */

export const SERVICE_TYPE = {
  REPAIR: 'REPAIR',
  INSTALL_SUPPORT: 'INSTALL_SUPPORT',
  TRAINING: 'TRAINING',
  INTERPRET: 'INTERPRET',
  TUNING: 'TUNING',
} as const;

export type ServiceType = (typeof SERVICE_TYPE)[keyof typeof SERVICE_TYPE];

export const SERVICE_TYPE_LABELS: Record<ServiceType, string> = {
  REPAIR: '수리',
  INSTALL_SUPPORT: '설치지원',
  TRAINING: '교육',
  INTERPRET: '통역',
  TUNING: '조건셋팅',
};

export const SERVICE_TYPE_OPTIONS: FilterOption[] = (
  Object.keys(SERVICE_TYPE_LABELS) as ServiceType[]
).map((t) => ({ value: t, label: SERVICE_TYPE_LABELS[t] }));

export const SERVICE_STATUS = {
  RECEIVED: 'RECEIVED',
  ASSIGNED: 'ASSIGNED',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
} as const;

export type ServiceStatus = (typeof SERVICE_STATUS)[keyof typeof SERVICE_STATUS];

export const SERVICE_STATUS_LABELS: Record<ServiceStatus, string> = {
  RECEIVED: '접수',
  ASSIGNED: '배정',
  IN_PROGRESS: '진행중',
  COMPLETED: '완료',
};

export const SERVICE_STATUS_OPTIONS: FilterOption[] = (
  Object.keys(SERVICE_STATUS_LABELS) as ServiceStatus[]
).map((s) => ({ value: s, label: SERVICE_STATUS_LABELS[s] }));

export const WARRANTY_DECISION = {
  UNDECIDED: 'UNDECIDED',
  FREE: 'FREE',
  PAID: 'PAID',
} as const;

export type WarrantyDecision = (typeof WARRANTY_DECISION)[keyof typeof WARRANTY_DECISION];

export const WARRANTY_DECISION_LABELS: Record<WarrantyDecision, string> = {
  UNDECIDED: '미확정',
  FREE: '무상',
  PAID: '유상',
};

export const WARRANTY_DECISION_OPTIONS: FilterOption[] = (
  Object.keys(WARRANTY_DECISION_LABELS) as WarrantyDecision[]
).map((w) => ({ value: w, label: WARRANTY_DECISION_LABELS[w] }));

export const SERVICE_EXPENSE_CATEGORY = {
  DAILY_WAGE: 'DAILY_WAGE',
  LODGING: 'LODGING',
  MEAL: 'MEAL',
  PARTS: 'PARTS',
  ETC: 'ETC',
} as const;

export type ServiceExpenseCategory =
  (typeof SERVICE_EXPENSE_CATEGORY)[keyof typeof SERVICE_EXPENSE_CATEGORY];

export const SERVICE_EXPENSE_CATEGORY_LABELS: Record<ServiceExpenseCategory, string> = {
  DAILY_WAGE: '일당',
  LODGING: '숙박비',
  MEAL: '식대',
  PARTS: '부품비',
  ETC: '기타',
};

export const EXPENSE_PAYER_TYPE = {
  COMPANY: 'COMPANY',
  ENGINEER: 'ENGINEER',
} as const;

export type ExpensePayerType = (typeof EXPENSE_PAYER_TYPE)[keyof typeof EXPENSE_PAYER_TYPE];

export const EXPENSE_PAYER_TYPE_LABELS: Record<ExpensePayerType, string> = {
  COMPANY: '회사 직접결제',
  ENGINEER: '엔지니어 청구',
};

export const ENGINEER_TYPE = {
  INTERNAL: 'INTERNAL',
  OUTSOURCED: 'OUTSOURCED',
  MANUFACTURER: 'MANUFACTURER',
} as const;

export type EngineerType = (typeof ENGINEER_TYPE)[keyof typeof ENGINEER_TYPE];

export const ENGINEER_TYPE_LABELS: Record<EngineerType, string> = {
  INTERNAL: '내부',
  OUTSOURCED: '외주',
  MANUFACTURER: '제조사',
};

export interface Engineer {
  id: number;
  name: string;
  type: EngineerType;
  affiliation: string | null;
  phone: string | null;
  employeeId: number | null;
  employeeName: string | null;
  active: boolean;
}

export interface EngineerRequest {
  name: string;
  type: EngineerType;
  affiliation: string | null;
  phone: string | null;
  employeeId: number | null;
  active: boolean;
}

export interface ServiceVisit {
  id: number;
  visitDate: string;
  engineerId: number;
  engineerName: string | null;
  problem: string | null;
  resolution: string | null;
}

export interface ServiceVisitRequest {
  visitDate: string;
  engineerId: number;
  problem: string | null;
  resolution: string | null;
}

export interface ServiceExpense {
  id: number;
  category: ServiceExpenseCategory;
  amount: number;
  payerType: ExpensePayerType;
  paidDate: string | null;
  engineerId: number | null;
  engineerName: string | null;
  note: string | null;
}

export interface ServiceExpenseRequest {
  category: ServiceExpenseCategory;
  amount: number;
  payerType: ExpensePayerType;
  paidDate: string | null;
  engineerId: number | null;
  note: string | null;
}

export interface AfterServiceSummary {
  id: number;
  receiptNo: string;
  customerId: number;
  customerName: string | null;
  equipmentId: number | null;
  equipmentModelName: string | null;
  equipmentSerialNo: string | null;
  receivedDate: string;
  type: ServiceType;
  status: ServiceStatus;
  assignedEngineerId: number | null;
  assignedEngineerName: string | null;
  warrantyDecision: WarrantyDecision;
  billingAmount: number | null;
  expenseTotal: number;
  completedDate: string | null;
}

export interface AfterServiceDetail {
  id: number;
  receiptNo: string;
  customerId: number;
  customerName: string | null;
  equipmentId: number | null;
  equipmentModelName: string | null;
  equipmentSerialNo: string | null;
  receivedDate: string;
  type: ServiceType;
  symptom: string | null;
  status: ServiceStatus;
  assignedEngineerId: number | null;
  assignedEngineerName: string | null;
  warrantyDecision: WarrantyDecision;
  billingAmount: number | null;
  completedDate: string | null;
  expenseTotal: number;
  visits: ServiceVisit[];
  expenses: ServiceExpense[];
}

export interface AfterServiceCreateRequest {
  /** 채번 규칙 AUTO 모드면 null — 시스템이 생성 */
  receiptNo: string | null;
  customerId: number;
  equipmentId: number | null;
  receivedDate: string;
  type: ServiceType;
  symptom: string | null;
  status: ServiceStatus;
  assignedEngineerId: number | null;
  warrantyDecision: WarrantyDecision;
  billingAmount: number | null;
  completedDate: string | null;
}

/** 접수번호는 발급 후 불변 — 수정 요청에 없음. */
export type AfterServiceUpdateRequest = Omit<AfterServiceCreateRequest, 'receiptNo'>;

export interface AfterServiceSearchParams {
  receiptNoKeyword?: string | null;
  customerId?: number | null;
  type?: string | null;
  status?: string | null;
  warrantyDecision?: string | null;
  engineerId?: number | null;
  receivedDateFrom?: string | null;
  receivedDateTo?: string | null;
  page: number;
  size?: number;
  sort?: string;
}

/** 목록 페이지 필터 state. page/size/sort 는 GenericList 가 관리하므로 제외. */
export type AfterServiceListFilters = Omit<AfterServiceSearchParams, 'page' | 'size' | 'sort'>;

export interface AfterServiceFormValues {
  /** 채번 규칙 AUTO / 잠금 상태면 빈 문자열 (BE 자동 채번) */
  receiptNo: string;
  /** 빈 문자열 = 미선택 */
  customerId: string;
  customerName: string;
  /** 빈 문자열 = 미연결 (대장 미등록 설비 접수 허용) */
  equipmentId: string;
  equipmentLabel: string;
  receivedDate: string;
  type: string;
  symptom: string;
  status: string;
  /** 빈 문자열 = 미배정 */
  assignedEngineerId: string;
  warrantyDecision: string;
  billingAmount: string;
  completedDate: string;
}

export const EMPTY_AFTER_SERVICE_FORM: AfterServiceFormValues = {
  receiptNo: '',
  customerId: '',
  customerName: '',
  equipmentId: '',
  equipmentLabel: '',
  receivedDate: '',
  type: SERVICE_TYPE.REPAIR,
  symptom: '',
  status: SERVICE_STATUS.RECEIVED,
  assignedEngineerId: '',
  warrantyDecision: WARRANTY_DECISION.UNDECIDED,
  billingAmount: '',
  completedDate: '',
};

/** 설비 표시 라벨 — 모델명 (시리얼) 조합. */
export function equipmentLabelOf(modelName: string | null, serialNo: string | null): string {
  const model = modelName ?? '모델 미상';
  return serialNo ? `${model} (${serialNo})` : model;
}

export function afterServiceDetailToFormValues(d: AfterServiceDetail): AfterServiceFormValues {
  return {
    receiptNo: d.receiptNo,
    customerId: String(d.customerId),
    customerName: d.customerName ?? '',
    equipmentId: d.equipmentId == null ? '' : String(d.equipmentId),
    equipmentLabel: d.equipmentId == null
      ? ''
      : equipmentLabelOf(d.equipmentModelName, d.equipmentSerialNo),
    receivedDate: d.receivedDate,
    type: d.type,
    symptom: d.symptom ?? '',
    status: d.status,
    assignedEngineerId: d.assignedEngineerId == null ? '' : String(d.assignedEngineerId),
    warrantyDecision: d.warrantyDecision,
    billingAmount: d.billingAmount == null ? '' : String(d.billingAmount),
    completedDate: d.completedDate ?? '',
  };
}

export function afterServiceFormToCreateRequest(v: AfterServiceFormValues): AfterServiceCreateRequest {
  return {
    receiptNo: v.receiptNo.trim() === '' ? null : v.receiptNo.trim(),
    ...afterServiceFormToUpdateRequest(v),
  };
}

export function afterServiceFormToUpdateRequest(v: AfterServiceFormValues): AfterServiceUpdateRequest {
  return {
    customerId: Number(v.customerId),
    equipmentId: v.equipmentId === '' ? null : Number(v.equipmentId),
    receivedDate: v.receivedDate,
    type: v.type as ServiceType,
    symptom: emptyToNull(v.symptom),
    status: v.status as ServiceStatus,
    assignedEngineerId: v.assignedEngineerId === '' ? null : Number(v.assignedEngineerId),
    warrantyDecision: v.warrantyDecision as WarrantyDecision,
    // 청구액은 유상 (PAID) 확정 건에만 유효 — 그외 판정은 값이 남아 있어도 null 로 전송 (BE 도 동일 정규화).
    billingAmount:
      v.warrantyDecision === WARRANTY_DECISION.PAID && v.billingAmount.trim() !== ''
        ? Number(v.billingAmount)
        : null,
    completedDate: emptyToNull(v.completedDate),
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
