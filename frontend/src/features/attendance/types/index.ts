import type { ApprovalLineEntry } from '@/shared/ui/ApprovalLineField';
import { todayIsoDate } from '@/shared/utils/date';

/** BE attendance.internal.entity.LeaveType 미러. */
export type LeaveType = 'ANNUAL' | 'HALF_DAY_AM' | 'HALF_DAY_PM' | 'SICK' | 'ETC';

export const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  ANNUAL: '연차',
  HALF_DAY_AM: '오전 반차',
  HALF_DAY_PM: '오후 반차',
  SICK: '병가',
  ETC: '기타',
};

export const LEAVE_TYPE_OPTIONS: { value: LeaveType; label: string }[] = [
  { value: 'ANNUAL', label: '연차' },
  { value: 'HALF_DAY_AM', label: '오전 반차' },
  { value: 'HALF_DAY_PM', label: '오후 반차' },
  { value: 'SICK', label: '병가' },
  { value: 'ETC', label: '기타' },
];

/** BE attendance.internal.entity.LeaveStatus 미러 — 결재 결과 콜백으로만 전이. */
export type LeaveStatus = 'IN_PROGRESS' | 'APPROVED' | 'REJECTED';

export const LEAVE_STATUS_LABELS: Record<LeaveStatus, string> = {
  IN_PROGRESS: '결재 중',
  APPROVED: '승인',
  REJECTED: '반려',
};

export const LEAVE_STATUS_OPTIONS: { value: LeaveStatus; label: string }[] = [
  { value: 'IN_PROGRESS', label: '결재 중' },
  { value: 'APPROVED', label: '승인' },
  { value: 'REJECTED', label: '반려' },
];

/** BE AttendanceResponse 미러. */
export interface Attendance {
  id: number;
  employeeId: number;
  employeeName: string;
  workDate: string;
  checkInAt: string | null;
  checkOutAt: string | null;
  checkInWithinRange: boolean;
  checkOutWithinRange: boolean;
}

/** BE CheckInRequest / CheckOutRequest 미러. */
export interface CheckInRequest {
  latitude: number;
  longitude: number;
}

export type CheckOutRequest = CheckInRequest;

/** 내 월별 출퇴근 조회 파라미터 — GET /api/v1/attendances/me. */
export interface MyMonthlyAttendanceParams {
  year: number;
  month: number;
}

/**
 * 전 직원 근태 현황 필터 — BE 는 year / month 필수.
 * 필터 UI 의 "전체" (null) 는 api 쪽에서 현재 년 / 월로 fallback 매핑한다.
 */
export interface AttendanceListFilters {
  year: number | null;
  month: number | null;
  employeeId: number | null;
}

export interface AttendanceSearchParams extends AttendanceListFilters {
  page: number;
  size?: number;
  sort?: string;
}

/** BE LeaveResponse 미러 — 유형 / 상태 라벨은 FE 라벨 맵 (LEAVE_TYPE_LABELS 등) 으로 표시. */
export interface Leave {
  id: number;
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  days: number;
  reason: string | null;
  status: LeaveStatus;
  approvalDocumentId: number | null;
  createdAt: string;
}

/** BE 관리자 휴가 검색 row (GET /api/v1/leaves) 미러. */
export interface LeaveSummary {
  id: number;
  employeeId: number;
  employeeName: string;
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  days: number;
  reason: string | null;
  status: LeaveStatus;
  approvalDocumentId: number | null;
  createdAt: string;
}

/** 관리자 휴가 현황 필터 — startDate / endDate 는 휴가 기간과의 겹침 검색. */
export interface LeaveListFilters {
  status: LeaveStatus | null;
  employeeId: number | null;
  startDate: string | null;
  endDate: string | null;
}

export interface LeaveSearchParams extends LeaveListFilters {
  page: number;
  size?: number;
  sort?: string;
}

/** BE LeaveBalanceResponse 미러. */
export interface LeaveBalance {
  year: number;
  grantedDays: number;
  usedDays: number;
  remainingDays: number;
}

/** BE 전 직원 잔여 연차 row (GET /api/v1/leaves/balances) 미러. */
export interface EmployeeLeaveBalance {
  employeeId: number;
  employeeName: string;
  year: number;
  grantedDays: number;
  usedDays: number;
  remainingDays: number;
}

/** BE 부여 조정 요청 (PUT /api/v1/leaves/balances/{employeeId}) 미러. */
export interface LeaveBalanceAdjustRequest {
  year: number;
  grantedDays: number;
}

/** BE LeaveCreateRequest 미러. */
export interface LeaveCreateRequest {
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  reason: string | null;
  approverIds: number[];
}

export interface LeaveFormValues {
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  reason: string;
  approvalLine: ApprovalLineEntry[];
}

/** 시작 / 종료일을 오늘로 초기화 — 날짜 의존이라 상수 대신 factory. */
export function emptyLeaveForm(): LeaveFormValues {
  const today = todayIsoDate();
  return {
    leaveType: 'ANNUAL',
    startDate: today,
    endDate: today,
    reason: '',
    approvalLine: [],
  };
}

export function leaveFormToCreateRequest(v: LeaveFormValues): LeaveCreateRequest {
  return {
    leaveType: v.leaveType,
    startDate: v.startDate,
    endDate: v.endDate,
    reason: emptyToNull(v.reason),
    approverIds: v.approvalLine.map((entry) => entry.employeeId),
  };
}

function emptyToNull(v: string): string | null {
  return v.trim() === '' ? null : v.trim();
}
