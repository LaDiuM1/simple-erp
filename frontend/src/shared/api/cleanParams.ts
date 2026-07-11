/**
 * 검색 파라미터에서 "조건 없음" 값 (null / undefined / '' / 빈 배열) 을 제거.
 * BE 는 파라미터 미전송 = 필터 미적용으로 해석한다.
 */
export function cleanParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params).filter(([, v]) => {
      if (v === undefined || v === null || v === '') return false;
      if (Array.isArray(v) && v.length === 0) return false;
      return true;
    }),
  ) as Partial<T>;
}
