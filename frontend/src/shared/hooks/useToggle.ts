import { useCallback, useState } from 'react';

/** boolean state + on/off/toggle 묶음. modal open/close, confirm dialog 등에서 보일러플레이트 정리. */
export function useToggle(initial = false) {
  const [value, setValue] = useState(initial);
  const on = useCallback(() => setValue(true), []);
  const off = useCallback(() => setValue(false), []);
  const toggle = useCallback(() => setValue((v) => !v), []);
  return [value, { on, off, toggle, set: setValue }] as const;
}
