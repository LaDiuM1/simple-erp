export type QueryPresentationPhase =
  | 'initial'
  | 'transition'
  | 'blocking-error'
  | 'ready'
  | 'refreshing'
  | 'refresh-error';

interface QueryPresentationInput<T> {
  data?: T;
  currentData: T | undefined;
  isFetching?: boolean;
  isError?: boolean;
  error?: unknown;
}

export interface QueryPresentation<T> {
  phase: QueryPresentationPhase;
  data?: T;
  error?: unknown;
  isPending: boolean;
  isBlockingError: boolean;
  isRefreshError: boolean;
}

/**
 * RTK Query 의 `data` 는 인자가 바뀐 직후에도 직전 결과를 유지한다.
 * 현재 인자의 결과인 `currentData` 만 화면에 표시해 직전 검색 조건의 결과가 섞이지 않게 한다.
 */
export function resolveQueryPresentation<T>(
  query: QueryPresentationInput<T>,
): QueryPresentation<T> {
  const currentData = query.currentData;
  const hasCurrentData = currentData !== undefined;

  if (query.isError) {
    if (!hasCurrentData) {
      return {
        phase: 'blocking-error',
        data: undefined,
        error: query.error,
        isPending: false,
        isBlockingError: true,
        isRefreshError: false,
      };
    }
    return {
      phase: 'refresh-error',
      data: currentData,
      error: query.error,
      isPending: false,
      isBlockingError: false,
      isRefreshError: true,
    };
  }

  if (!hasCurrentData) {
    return {
      phase: query.data !== undefined ? 'transition' : 'initial',
      isPending: true,
      isBlockingError: false,
      isRefreshError: false,
    };
  }

  if (query.isFetching) {
    return {
      phase: 'refreshing',
      data: currentData,
      isPending: false,
      isBlockingError: false,
      isRefreshError: false,
    };
  }

  return {
    phase: 'ready',
    data: currentData,
    isPending: false,
    isBlockingError: false,
    isRefreshError: false,
  };
}
