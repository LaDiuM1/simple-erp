import { useCallback, useState } from 'react';
import type { FormState } from './types';

/**
 * GenericForm 내부 전용 폼 값 상태 훅.
 * 단순 useState 래퍼 — 키 단위 업데이트 / 전체 교체 / 초기값 복원 지원.
 */
export function useFormState<TValues extends object>(initial: TValues): FormState<TValues> {
  const [values, setValues] = useState<TValues>(initial);

  const updateField = useCallback(
    <K extends keyof TValues>(key: K, value: TValues[K]) => {
      setValues((prev) => ({ ...prev, [key]: value }));
    },
    [],
  );

  const setAll = useCallback((next: TValues) => {
    setValues(next);
  }, []);

  const reset = useCallback(() => {
    setValues(initial);
  }, [initial]);

  return { values, updateField, setAll, reset };
}
