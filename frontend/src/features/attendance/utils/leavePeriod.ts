export const MAX_LEAVE_PERIOD_SPAN_DAYS = 365;

export type LeavePeriodError =
  | 'REQUIRED'
  | 'INVALID'
  | 'END_BEFORE_START'
  | 'DIFFERENT_YEAR'
  | 'TOO_LONG';

const DAY_MILLIS = 24 * 60 * 60 * 1000;

function parseIsoDate(value: string): { year: number; timestamp: number } | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);
  if (!match) return null;

  const year = Number(match[1]);
  const month = Number(match[2]);
  const day = Number(match[3]);
  const timestamp = Date.UTC(year, month - 1, day);
  const date = new Date(timestamp);
  if (
    date.getUTCFullYear() !== year
    || date.getUTCMonth() !== month - 1
    || date.getUTCDate() !== day
  ) {
    return null;
  }
  return { year, timestamp };
}

/** LeaveService의 동일 연도 및 최대 기간 계약을 브라우저 제출 전에 동일하게 검증한다. */
export function validateLeavePeriod(startDate: string, endDate: string): LeavePeriodError | null {
  if (startDate === '' || endDate === '') return 'REQUIRED';

  const start = parseIsoDate(startDate);
  const end = parseIsoDate(endDate);
  if (!start || !end) return 'INVALID';
  if (end.timestamp < start.timestamp) return 'END_BEFORE_START';
  if (start.year !== end.year) return 'DIFFERENT_YEAR';
  if ((end.timestamp - start.timestamp) / DAY_MILLIS >= MAX_LEAVE_PERIOD_SPAN_DAYS) {
    return 'TOO_LONG';
  }
  return null;
}
