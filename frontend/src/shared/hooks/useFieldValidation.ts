import { useState } from 'react';

export type FieldValidator<TValues, K extends keyof TValues> =
  (value: TValues[K], all: TValues) => string | null;

export type ValidatorMap<TValues> = {
  [K in keyof TValues]?: FieldValidator<TValues, K>;
};

export interface FieldValidation<TValues> {
  /** 필드 onBlur 핸들러 — touched 마킹 */
  onBlur: (key: keyof TValues) => () => void;
  /** touched 이고 에러일 때만 메시지 반환, 아니면 undefined */
  errorMessage: (key: keyof TValues) => string | undefined;
  /** TextField 의 error prop 용 boolean */
  isInvalid: (key: keyof TValues) => boolean;
  /** submit 시 호출 — 모든 validators 키를 touched 처리하고 통과 여부 반환 */
  validateAll: () => boolean;
}

/**
 * 필드 단위 유효성 + touched 상태 관리 훅.
 * 입력 중에는 에러를 숨기고 blur 시점부터 표시. submit 시 validateAll() 로 일괄 검증.
 *
 * 다른 필드 값을 참조해야 하면 validator 의 두 번째 인자 `all` 사용 (예: 비밀번호 확인).
 */
export function useFieldValidation<T extends object>(
  values: T,
  validators: ValidatorMap<T>,
): FieldValidation<T> {
  const [touched, setTouched] = useState<Partial<Record<keyof T, boolean>>>({});

  // 매 렌더마다 derive — React Compiler 가 적절히 메모이제이션
  const errors: Partial<Record<keyof T, string | null>> = {};
  for (const key of Object.keys(validators) as Array<keyof T>) {
    const validator = validators[key];
    if (validator) {
      errors[key] = (validator as (v: unknown, all: T) => string | null)(
        values[key],
        values,
      );
    }
  }

  return {
    onBlur: (key) => () => {
      setTouched((prev) => (prev[key] ? prev : { ...prev, [key]: true }));
    },
    errorMessage: (key) => (touched[key] ? errors[key] ?? undefined : undefined),
    isInvalid: (key) => Boolean(touched[key] && errors[key]),
    validateAll: () => {
      const all: Partial<Record<keyof T, boolean>> = {};
      (Object.keys(validators) as Array<keyof T>).forEach((k) => {
        all[k] = true;
      });
      setTouched(all);
      return Object.values(errors).every((e) => !e);
    },
  };
}
