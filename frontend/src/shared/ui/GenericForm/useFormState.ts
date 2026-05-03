import { useCallback, useState } from 'react';
import type { FormState } from './types';

/**
 * 폼 값 상태 훅. 키 단위 업데이트 / 전체 교체 / 초기값 복원.
 * GenericForm 외에도 custom 폼 hook 들이 재사용. lazy initializer 도 지원.
 */
export function useFormState<TValues extends object>(
  initial: TValues | (() => TValues),
): FormState<TValues> {
  const [values, setValues] = useState<TValues>(initial);

  const updateField = useCallback(
    <K extends keyof TValues>(key: K, value: TValues[K]) => {
      setValues((prev) => ({ ...prev, [key]: value }));
    },
    [],
  );

  const setAll = useCallback((next: TValues | ((prev: TValues) => TValues)) => {
    setValues(next);
  }, []);

  const reset = useCallback(() => {
    setValues(typeof initial === 'function' ? (initial as () => TValues)() : initial);
  }, [initial]);

  return { values, updateField, setAll, reset };
}
