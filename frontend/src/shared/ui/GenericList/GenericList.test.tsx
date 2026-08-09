import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterAll, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import type { PageResponse } from '@/shared/types/api';
import GenericList from './GenericList';
import type { ListApiConfig, QueryState } from './types';

const mocks = vi.hoisted(() => ({
  snackbarError: vi.fn(),
  snackbarSuccess: vi.fn(),
}));

vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ canWrite: true }),
}));
vi.mock('@/shared/ui/feedback/snackbar', () => ({
  useSnackbar: () => ({ error: mocks.snackbarError, success: mocks.snackbarSuccess }),
}));
vi.mock('@mui/material/useMediaQuery', () => ({ default: () => false }));

beforeAll(() => {
  vi.stubGlobal('ResizeObserver', class {
    observe() {}
    disconnect() {}
  });
});

afterAll(() => vi.unstubAllGlobals());

interface Row {
  id: number;
  name: string;
}

interface Filters {
  keyword: string | null;
}

const row = (id: number, name: string): Row => ({ id, name });

const page = (
  content: Row[],
  currentPage = 0,
  totalPages = 1,
): PageResponse<Row> => ({
  content,
  page: currentPage,
  size: 10,
  totalElements: content.length,
  totalPages,
  hasNext: currentPage + 1 < totalPages,
});

function createApi(
  useList: (params: Filters & { page: number; size: number; sort: string }) => QueryState<Row>,
): ListApiConfig<Row, Filters> {
  return {
    menuCode: 'CUSTOMERS',
    useList,
    rowKey: (item) => item.id,
    emptyMessage: '고객사가 없습니다.',
  };
}

describe('GenericList query presentation', () => {
  beforeEach(() => {
    mocks.snackbarError.mockReset();
    mocks.snackbarSuccess.mockReset();
  });

  it('다음 페이지가 준비되기 전에는 직전 행·건수·빈 상태를 모두 숨긴다', async () => {
    const user = userEvent.setup();
    const previous = page([row(1, '이전 고객사')], 0, 3);
    const api = createApi((params) => params.page === 0
      ? {
          data: previous,
          currentData: previous,
          isFetching: false,
          isError: false,
          refetch: vi.fn(),
        }
      : {
          data: previous,
          currentData: undefined,
          isFetching: true,
          isError: false,
          refetch: vi.fn(),
        });

    renderWithTheme(
      <GenericList
        api={api}
        searchFilter={[]}
        column={[{ key: 'name', label: '고객사' }]}
      />,
    );

    await user.click(screen.getByRole('button', { name: 'Go to page 3' }));

    expect(screen.queryByText('이전 고객사')).not.toBeInTheDocument();
    expect(screen.queryByText('총 1건')).not.toBeInTheDocument();
    expect(screen.queryByText('고객사가 없습니다.')).not.toBeInTheDocument();
    expect(screen.getByRole('status', { name: '목록 불러오는 중' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'page 3' })).toBeDisabled();
  });

  it('요청한 페이지의 차단 오류에서도 목록 shell과 유효한 페이지 번호를 유지한다', async () => {
    const user = userEvent.setup();
    const previous = page([row(1, '이전 고객사')], 0, 3);
    const refetch = vi.fn();
    const api = createApi((params) => params.page === 0
      ? {
          data: previous,
          currentData: previous,
          isFetching: false,
          isError: false,
          refetch,
        }
      : {
          data: previous,
          currentData: undefined,
          isFetching: false,
          isError: true,
          error: { status: 500, message: '페이지 조회 실패' },
          refetch,
        });

    renderWithTheme(
      <GenericList
        api={api}
        searchFilter={[]}
        column={[{ key: 'name', label: '고객사' }]}
      />,
    );
    await user.click(screen.getByRole('button', { name: 'Go to page 3' }));

    expect(screen.getByRole('columnheader', { name: '고객사' })).toBeInTheDocument();
    expect(screen.getByText('페이지 조회 실패')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'page 3' })).toBeDisabled();
    expect(screen.queryByText('이전 고객사')).not.toBeInTheDocument();
  });

  it('오류 본문이 없어도 차단 오류를 빈 목록으로 오해하지 않는다', () => {
    const api = createApi(() => ({
      data: undefined,
      currentData: undefined,
      isFetching: false,
      isError: true,
      error: undefined,
      refetch: vi.fn(),
    }));

    renderWithTheme(
      <GenericList
        api={api}
        searchFilter={[]}
        column={[{ key: 'name', label: '고객사' }]}
      />,
    );

    expect(screen.getByRole('alert')).toHaveTextContent('목록을 불러오지 못했습니다.');
    expect(screen.queryByText('고객사가 없습니다.')).not.toBeInTheDocument();
  });

  it('백그라운드 갱신은 현재 행을 유지하고 얇은 진행 상태만 표시한다', () => {
    const current = page([row(2, '현재 고객사')]);
    const api = createApi(() => ({
      data: current,
      currentData: current,
      isFetching: true,
      isError: false,
      refetch: vi.fn(),
    }));

    renderWithTheme(
      <GenericList
        api={api}
        searchFilter={[]}
        column={[{ key: 'name', label: '고객사' }]}
      />,
    );

    expect(screen.getByText('현재 고객사')).toBeInTheDocument();
    expect(screen.getByRole('progressbar', { name: '목록 갱신 중' })).toBeInTheDocument();
    expect(screen.queryByRole('status', { name: '목록 불러오는 중' })).not.toBeInTheDocument();
  });

  it('갱신 실패는 현재 행과 명시적인 재시도 안내를 함께 유지한다', async () => {
    const user = userEvent.setup();
    const refetch = vi.fn();
    const current = page([row(2, '현재 고객사')]);
    const api = createApi(() => ({
      data: current,
      currentData: current,
      isFetching: false,
      isError: true,
      error: { status: 500, message: '갱신 실패' },
      refetch,
    }));

    renderWithTheme(
      <GenericList
        api={api}
        searchFilter={[]}
        column={[{ key: 'name', label: '고객사' }]}
      />,
    );

    expect(screen.getByText('현재 고객사')).toBeInTheDocument();
    expect(screen.getByText('갱신 실패')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(refetch).toHaveBeenCalledOnce();
  });
});
