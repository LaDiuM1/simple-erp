/** 오늘 날짜를 date input 값 형식 ('YYYY-MM-DD', 로컬 기준) 으로 반환. */
export function todayIsoDate(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${now.getFullYear()}-${month}-${day}`;
}
