import type { ReactNode } from 'react';
import LinearProgress from '@mui/material/LinearProgress';
import { getErrorMessage } from '@/shared/api/error';
import { resolveQueryPresentation } from '@/shared/api/queryPresentation';
import LoadingScreen from './LoadingScreen';
import ErrorScreen from './ErrorScreen';
import RefreshErrorNotice from './RefreshErrorNotice';

interface QueryLike<T> {
  data?: T;
  /** RTK Query 상태 union에서는 optional이지만, 표시 판단에는 이 값만 사용한다. */
  currentData?: T;
  isLoading: boolean;
  isFetching?: boolean;
  isError?: boolean;
  error?: unknown;
  refetch?: () => void;
}

type Queries = Record<string, QueryLike<unknown> | undefined>;

type Unwrapped<Qs extends Queries> = {
  [K in keyof Qs]: Exclude<Qs[K], undefined> extends QueryLike<infer T> ? T : never;
};

interface QueryGateProps<Qs extends Queries> {
  queries: Qs;
  children: (data: Unwrapped<Qs>) => ReactNode;
  fullScreen?: boolean;
}

export default function QueryGate<Qs extends Queries>({
  queries,
  children,
  fullScreen = false,
}: QueryGateProps<Qs>) {
  const list = Object.values(queries).filter(
    (query): query is QueryLike<unknown> => query !== undefined,
  );

  const entries = list.map((query) => ({
    query,
    presentation: resolveQueryPresentation({ ...query, currentData: query.currentData }),
  }));
  const refetchAll = () => {
    list.forEach((q) => q.refetch?.());
  };
  const blocked = entries.find(({ presentation }) => presentation.isBlockingError);
  if (blocked) {
    return (
      <ErrorScreen
        message={getErrorMessage(blocked.presentation.error)}
        onRetry={refetchAll}
        fullScreen={fullScreen}
      />
    );
  }

  const stillLoading = entries.some(({ presentation }) => presentation.isPending);
  if (stillLoading) {
    return <LoadingScreen fullScreen={fullScreen} />;
  }

  const unwrapped = Object.fromEntries(
    Object.entries(queries)
      .filter((entry): entry is [string, QueryLike<unknown>] => entry[1] !== undefined)
      .map(([key, query]) => [
        key,
        resolveQueryPresentation({ ...query, currentData: query.currentData }).data,
      ]),
  ) as Unwrapped<Qs>;
  const refreshError = entries.find(({ presentation }) => presentation.isRefreshError);
  const isRefreshing = entries.some(({ presentation }) => presentation.phase === 'refreshing');

  return (
    <>
      {refreshError && (
        <RefreshErrorNotice error={refreshError.presentation.error} onRetry={refetchAll} />
      )}
      {isRefreshing && <LinearProgress aria-label="내용 갱신 중" sx={{ height: 2 }} />}
      {children(unwrapped)}
    </>
  );
}
