import type { ReactNode } from 'react';
import { getErrorMessage } from '@/shared/api/error';
import LoadingScreen from './LoadingScreen';
import ErrorScreen from './ErrorScreen';

interface QueryLike<T> {
  data?: T;
  isLoading: boolean;
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

  const errored = list.find((q) => q.isError);
  if (errored) {
    const refetchAll = () => {
      list.forEach((q) => q.refetch?.());
    };
    return (
      <ErrorScreen
        message={getErrorMessage(errored.error)}
        onRetry={refetchAll}
        fullScreen={fullScreen}
      />
    );
  }

  const stillLoading = list.some((q) => q.isLoading || q.data === undefined);
  if (stillLoading) {
    return <LoadingScreen />;
  }

  const unwrapped = Object.fromEntries(
    Object.entries(queries)
      .filter((entry): entry is [string, QueryLike<unknown>] => entry[1] !== undefined)
      .map(([key, query]) => [key, query.data]),
  ) as Unwrapped<Qs>;

  return <>{children(unwrapped)}</>;
}
