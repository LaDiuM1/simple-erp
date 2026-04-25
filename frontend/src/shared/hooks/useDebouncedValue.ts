import { useEffect, useState } from 'react';

/**
 * 입력값의 변동이 잦을 때 마지막 변경으로부터 일정 시간이 지난 후의 값을 반환.
 * 자동완성 / 가용성 검사 등 burst 호출을 줄이는 용도.
 */
export function useDebouncedValue<T>(value: T, delayMs = 400): T {
  const [debounced, setDebounced] = useState<T>(value);

  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(id);
  }, [value, delayMs]);

  return debounced;
}
