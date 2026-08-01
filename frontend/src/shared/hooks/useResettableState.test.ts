import { act, renderHook } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import { modalStateResetKey, useResettableState } from './useResettableState';

describe('useResettableState', () => {
  it('같은 의미 키의 새 객체에는 작성 상태를 보존하고 키가 바뀔 때만 초기화한다', () => {
    const { result, rerender } = renderHook(
      ({ entity }) => useResettableState(
        modalStateResetKey(true, entity.id),
        () => entity.name,
      ),
      { initialProps: { entity: { id: 1, name: '처음 값' } } },
    );

    act(() => result.current[1]('작성 중'));
    rerender({ entity: { id: 1, name: 'refetch 값' } });
    expect(result.current[0]).toBe('작성 중');

    rerender({ entity: { id: 2, name: '다른 엔티티' } });
    expect(result.current[0]).toBe('다른 엔티티');
  });

  it('같은 엔티티도 모달을 닫았다 다시 열면 초기화한다', () => {
    const { result, rerender } = renderHook(
      ({ open }) => useResettableState(
        modalStateResetKey(open, 1),
        () => '초기 값',
      ),
      { initialProps: { open: true } },
    );

    act(() => result.current[1]('작성 중'));
    rerender({ open: false });
    rerender({ open: true });

    expect(result.current[0]).toBe('초기 값');
  });
});
