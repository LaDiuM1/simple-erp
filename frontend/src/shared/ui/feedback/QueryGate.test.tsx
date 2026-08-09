import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import QueryGate from './QueryGate';

interface Detail {
  name: string;
}

function renderGate(query: {
  data?: Detail;
  currentData?: Detail;
  isLoading: boolean;
  isFetching?: boolean;
  isError?: boolean;
  error?: unknown;
  refetch?: () => void;
}, fullScreen = false) {
  return renderWithTheme(
    <QueryGate queries={{ detail: query }} fullScreen={fullScreen}>
      {({ detail }) => <div>{detail.name}</div>}
    </QueryGate>,
  );
}

describe('QueryGate', () => {
  it('인자가 바뀌는 동안 직전 결과를 숨긴다', () => {
    renderGate({
      data: { name: '이전 상세' },
      currentData: undefined,
      isLoading: false,
      isFetching: true,
    });

    expect(screen.getByText('불러오는 중...')).toBeInTheDocument();
    expect(screen.queryByText('이전 상세')).not.toBeInTheDocument();
    const loading = getComputedStyle(screen.getByRole('status', { name: '내용 불러오는 중' }));
    expect(loading.minHeight).toBe('400px');
    expect(loading.backgroundColor).toBe('rgba(0, 0, 0, 0)');
  });

  it('현재 결과를 유지한 채 갱신 상태를 알린다', () => {
    renderGate({
      data: { name: '현재 상세' },
      currentData: { name: '현재 상세' },
      isLoading: false,
      isFetching: true,
    });

    expect(screen.getByText('현재 상세')).toBeInTheDocument();
    expect(screen.getByRole('progressbar', { name: '내용 갱신 중' })).toBeInTheDocument();
  });

  it('갱신 실패 시 현재 결과와 재시도 안내를 함께 유지한다', async () => {
    const user = userEvent.setup();
    const refetch = vi.fn();
    renderGate({
      data: { name: '현재 상세' },
      currentData: { name: '현재 상세' },
      isLoading: false,
      isError: true,
      error: { status: 500, message: '갱신 실패' },
      refetch,
    });

    expect(screen.getByText('현재 상세')).toBeInTheDocument();
    expect(screen.getByText('갱신 실패')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(refetch).toHaveBeenCalledOnce();
  });

  it('현재 결과가 없는 오류는 직전 결과 대신 차단 오류를 표시한다', () => {
    renderGate({
      data: { name: '이전 상세' },
      currentData: undefined,
      isLoading: false,
      isError: true,
      error: { status: 500, message: '조회 실패' },
    });

    expect(screen.getByText('조회 실패')).toBeInTheDocument();
    expect(screen.getByRole('alert')).toHaveTextContent('조회 실패');
    expect(screen.queryByText('이전 상세')).not.toBeInTheDocument();
  });
});
