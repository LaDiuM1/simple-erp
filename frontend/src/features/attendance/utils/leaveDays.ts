import type { LeaveType } from '@/features/attendance/types';

/** BE LeaveType.isHalfDay() 미러. */
export function isHalfDayLeaveType(type: LeaveType): boolean {
  return type === 'HALF_DAY_AM' || type === 'HALF_DAY_PM';
}

/** BE LeaveType.deductible 미러 — true 유형만 연차 잔여에서 차감. */
export function isDeductibleLeaveType(type: LeaveType): boolean {
  return type === 'ANNUAL' || isHalfDayLeaveType(type);
}

/**
 * 예상 차감 일수 — BE LeaveService.calculateDays + deductible 정책 미러 (즉시 피드백용, 최종 검증은 BE).
 * 반차 = 0.5 고정, 연차 = 주말 (토 / 일) 제외 일수, 병가 / 기타 = 차감 없음 (0).
 */
export function calculateDeductedDays(type: LeaveType, startDate: string, endDate: string): number {
  if (!isDeductibleLeaveType(type)) return 0;
  if (isHalfDayLeaveType(type)) return 0.5;
  if (startDate === '' || endDate === '') return 0;

  const start = parseIsoDate(startDate);
  const end = parseIsoDate(endDate);
  if (start.getTime() > end.getTime()) return 0;

  let count = 0;
  const cursor = new Date(start);
  while (cursor.getTime() <= end.getTime()) {
    const weekday = cursor.getDay();
    if (weekday !== 0 && weekday !== 6) count += 1;
    cursor.setDate(cursor.getDate() + 1);
  }
  return count;
}

function parseIsoDate(date: string): Date {
  const [year, month, day] = date.split('-').map(Number);
  return new Date(year, month - 1, day);
}
