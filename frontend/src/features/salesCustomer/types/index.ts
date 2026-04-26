export type SalesActivityType = 'VISIT' | 'CALL' | 'MEETING' | 'EMAIL' | 'OTHER';

export const SALES_ACTIVITY_TYPE_LABELS: Record<SalesActivityType, string> = {
  VISIT: '방문',
  CALL: '전화',
  MEETING: '미팅',
  EMAIL: '이메일',
  OTHER: '기타',
};

export const SALES_ACTIVITY_TYPE_OPTIONS: { value: SalesActivityType; label: string }[] = [
  { value: 'VISIT', label: '방문' },
  { value: 'CALL', label: '전화' },
  { value: 'MEETING', label: '미팅' },
  { value: 'EMAIL', label: '이메일' },
  { value: 'OTHER', label: '기타' },
];

export interface SalesActivity {
  id: number;
  customerId: number;
  type: SalesActivityType;
  activityDate: string;
  subject: string;
  content: string | null;
  ourEmployeeId: number;
  ourEmployeeName: string | null;
  ourEmployeeDepartmentName: string | null;
  customerContactName: string | null;
  customerContactPosition: string | null;
}

export interface SalesAssignment {
  id: number;
  customerId: number;
  employeeId: number;
  employeeName: string | null;
  employeeDepartmentName: string | null;
  employeePositionName: string | null;
  startDate: string;
  endDate: string | null;
  primary: boolean;
  reason: string | null;
  active: boolean;
}

export interface SalesCustomerAggregate {
  customerId: number;
  primaryAssigneeId: number | null;
  primaryAssigneeName: string | null;
  activeAssigneeCount: number;
  activityCount: number;
  lastActivityDate: string | null;
}

export interface SalesCustomerDetail {
  customerId: number;
  customerCode: string;
  customerName: string;
  activities: SalesActivity[];
  assignments: SalesAssignment[];
}

export interface SalesActivityCreateRequest {
  customerId: number;
  type: SalesActivityType;
  activityDate: string;
  subject: string;
  content?: string | null;
  ourEmployeeId: number;
  customerContactName?: string | null;
  customerContactPosition?: string | null;
}

export type SalesActivityUpdateRequest = Omit<SalesActivityCreateRequest, 'customerId'>;

export interface SalesAssignmentCreateRequest {
  customerId: number;
  employeeId: number;
  startDate: string;
  primary: boolean;
  reason?: string | null;
}

export interface SalesAssignmentUpdateRequest {
  startDate: string;
  primary: boolean;
  reason?: string | null;
}

export interface SalesAssignmentTerminateRequest {
  endDate: string;
  reason?: string | null;
}

/** datetime-local 입력값 (YYYY-MM-DDTHH:mm) ↔ ISO 변환 헬퍼. */
export function todayDateTimeLocal(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

export function todayIsoDate(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

/** ISO datetime ("2026-04-20T10:00:00") → datetime-local 입력값 ("2026-04-20T10:00"). */
export function isoToDateTimeLocal(iso: string): string {
  return iso.length >= 16 ? iso.slice(0, 16) : iso;
}
