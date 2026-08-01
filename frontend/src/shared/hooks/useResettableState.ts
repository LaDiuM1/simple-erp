import { useState, type Dispatch, type SetStateAction } from 'react';

export type StateResetKey = string | number | boolean | null | undefined;

/**
 * 로컬 상태를 안정적인 의미 키에 연결한다.
 *
 * 모달처럼 컴포넌트는 유지한 채 다른 엔티티를 편집하는 화면에서 객체 참조 자체를
 * reset 조건으로 사용하면 refetch 만으로 작성 중인 값이 사라질 수 있다. 호출자는
 * `modalStateResetKey(open, entity.id)` 같이 실제 초기화 경계를 나타내는 키를 넘긴다.
 */
export function useResettableState<T>(
  resetKey: StateResetKey,
  createInitialState: () => T,
): [T, Dispatch<SetStateAction<T>>] {
  const [state, setState] = useState<T>(createInitialState);
  const [previousResetKey, setPreviousResetKey] = useState(resetKey);

  if (!Object.is(previousResetKey, resetKey)) {
    setPreviousResetKey(resetKey);
    setState(createInitialState());
  }

  return [state, setState];
}

/** 닫힘/신규/수정 모드를 충돌 없는 단일 reset key 로 변환한다. */
export function modalStateResetKey(
  open: boolean,
  entityKey: StateResetKey,
): string {
  return open ? `open:${entityKey ?? 'new'}` : 'closed';
}
