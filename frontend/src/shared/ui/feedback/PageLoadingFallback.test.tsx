import { StrictMode } from 'react';
import { act, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import PageLoadingFallback from './PageLoadingFallback';

describe('PageLoadingFallback', () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it('짧은 대기에는 로딩 표시를 노출하지 않는다', () => {
    vi.useFakeTimers();
    const view = renderWithTheme(<PageLoadingFallback />);

    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    expect(screen.queryByText('페이지를 불러오는 중입니다.')).not.toBeInTheDocument();
    const status = screen.getByRole('status');
    const busyBoundary = view.container.querySelector<HTMLElement>('[aria-busy="true"]');
    expect(status).toBeEmptyDOMElement();
    expect(busyBoundary).not.toContainElement(status);

    act(() => vi.advanceTimersByTime(199));

    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
    expect(status).toBeEmptyDOMElement();
  });

  it('대기가 계속될 때만 숨김 상태를 보조 기술에 전달한다', () => {
    vi.useFakeTimers();
    const view = renderWithTheme(<PageLoadingFallback />);

    expect(view.container.firstElementChild).toHaveAttribute('aria-busy', 'true');

    act(() => vi.advanceTimersByTime(200));

    const status = screen.getByRole('status');
    const style = getComputedStyle(status);
    expect(status).toBeInTheDocument();
    expect(status).toHaveAttribute('aria-atomic', 'true');
    expect(style.position).toBe('absolute');
    expect(style.width).toBe('1px');
    expect(style.height).toBe('1px');
    expect(style.overflow).toBe('hidden');
    expect(status).toHaveTextContent('페이지 불러오는 중');
    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
  });

  it('대기 경계가 사라지면 예약한 표시를 취소한다', () => {
    vi.useFakeTimers();
    const view = renderWithTheme(<PageLoadingFallback />);

    expect(screen.getByRole('status')).toBeEmptyDOMElement();
    expect(vi.getTimerCount()).toBe(1);

    view.unmount();

    expect(vi.getTimerCount()).toBe(0);
    act(() => vi.advanceTimersByTime(200));
    expect(screen.queryByRole('status')).not.toBeInTheDocument();
  });

  it('StrictMode 재실행에서도 활성 표시 예약을 하나만 유지한다', () => {
    vi.useFakeTimers();
    renderWithTheme(
      <StrictMode>
        <PageLoadingFallback />
      </StrictMode>,
    );

    expect(vi.getTimerCount()).toBe(1);
    expect(screen.getByRole('status')).toBeEmptyDOMElement();

    act(() => vi.advanceTimersByTime(200));

    expect(screen.getAllByRole('status')).toHaveLength(1);
    expect(screen.getByRole('status')).toHaveTextContent('페이지 불러오는 중');
    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();
  });
});
