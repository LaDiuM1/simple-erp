import { describe, expect, it } from 'vitest';
import { resolveQueryPresentation } from './queryPresentation';

describe('resolveQueryPresentation', () => {
  const current = { id: 2 };
  const previous = { id: 1 };

  it.each([
    {
      name: '최초 조회',
      query: { data: undefined, currentData: undefined, isLoading: true, isFetching: true },
      phase: 'initial',
      data: undefined,
    },
    {
      name: '검색 조건 전환',
      query: { data: previous, currentData: undefined, isLoading: false, isFetching: true },
      phase: 'transition',
      data: undefined,
    },
    {
      name: '현재 결과 준비',
      query: { data: current, currentData: current, isLoading: false, isFetching: false },
      phase: 'ready',
      data: current,
    },
    {
      name: '현재 결과 갱신',
      query: { data: current, currentData: current, isLoading: false, isFetching: true },
      phase: 'refreshing',
      data: current,
    },
  ])('$name 상태를 구분한다', ({ query, phase, data }) => {
    const result = resolveQueryPresentation(query);

    expect(result.phase).toBe(phase);
    expect(result.data).toBe(data);
  });

  it('현재 결과가 없는 오류는 직전 결과를 숨기고 차단한다', () => {
    const error = { status: 500, message: '조회 실패' };

    const result = resolveQueryPresentation({
      data: previous,
      currentData: undefined,
      isFetching: false,
      isError: true,
      error,
    });

    expect(result).toMatchObject({
      phase: 'blocking-error',
      data: undefined,
      error,
      isBlockingError: true,
    });
  });

  it('현재 결과를 보유한 갱신 오류는 화면을 유지한다', () => {
    const error = { status: 500, message: '갱신 실패' };

    const result = resolveQueryPresentation({
      data: current,
      currentData: current,
      isFetching: false,
      isError: true,
      error,
    });

    expect(result).toMatchObject({
      phase: 'refresh-error',
      data: current,
      error,
      isRefreshError: true,
    });
  });

});
