/**
 * 폼 값 객체의 string 값을 재귀적으로 trim 한 새 객체 반환.
 * 제출 시점에 한 번만 호출 — controlled input 의 표시값은 그대로 두고 서버 전송 페이로드만 정리.
 * 배열 / 중첩 객체도 따라 들어가며, skipKeys 에 명시한 최상위 키는 원본 유지 (비밀번호 등).
 */
export function trimStringValues<T extends object>(
  values: T,
  options: { skipKeys?: ReadonlyArray<keyof T & string> } = {},
): T {
  const skip = new Set<string>(options.skipKeys ?? []);
  const out: Record<string, unknown> = {};
  for (const [key, value] of Object.entries(values)) {
    out[key] = skip.has(key) ? value : trimDeep(value);
  }
  return out as T;
}

function trimDeep(v: unknown): unknown {
  if (typeof v === 'string') return v.trim();
  if (Array.isArray(v)) return v.map(trimDeep);
  if (v !== null && typeof v === 'object') {
    const out: Record<string, unknown> = {};
    for (const [k, val] of Object.entries(v)) {
      out[k] = trimDeep(val);
    }
    return out;
  }
  return v;
}
