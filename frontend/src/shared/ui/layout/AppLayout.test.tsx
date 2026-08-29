import { lazy, type ReactElement } from 'react';
import { act, fireEvent, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import BusinessRoundedIcon from '@mui/icons-material/BusinessRounded';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import KpiCard from '@/features/dashboard/components/KpiCard/KpiCard';
import { renderWithTheme } from '@/test/renderWithTheme';
import AppLayout from './AppLayout';

const mocks = vi.hoisted(() => ({
  dispatch: vi.fn(),
  snackbarSuccess: vi.fn(),
  useGetMyProfileQuery: vi.fn(() => ({ data: undefined })),
}));

vi.mock('@/app/hooks', () => ({
  useAppDispatch: () => mocks.dispatch,
}));

vi.mock('@/features/auth/store/authActions', () => ({
  performLogout: vi.fn(),
}));

vi.mock('@/features/employee/api/employeeApi', () => ({
  useGetMyProfileQuery: mocks.useGetMyProfileQuery,
}));

vi.mock('@/shared/ui/feedback/snackbar', () => ({
  useSnackbar: () => ({ success: mocks.snackbarSuccess }),
}));

vi.mock('@/shared/ui/feedback/ConfirmModal', () => ({
  default: () => null,
}));

describe('AppLayout route transition', () => {
  let nextFrameId: number;
  let frameCallbacks: Map<number, FrameRequestCallback>;

  beforeEach(() => {
    vi.clearAllMocks();
    nextFrameId = 1;
    frameCallbacks = new Map();
    vi.stubGlobal('requestAnimationFrame', vi.fn((callback: FrameRequestCallback) => {
      const frameId = nextFrameId;
      nextFrameId += 1;
      frameCallbacks.set(frameId, callback);
      return frameId;
    }));
    vi.stubGlobal('cancelAnimationFrame', vi.fn((frameId: number) => {
      frameCallbacks.delete(frameId);
    }));
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    vi.useRealTimers();
  });

  function runNextFrame(timestamp: number) {
    const entry = frameCallbacks.entries().next().value as [number, FrameRequestCallback] | undefined;
    if (!entry) throw new Error('예약된 animation frame 이 없습니다.');
    const [frameId, callback] = entry;
    frameCallbacks.delete(frameId);
    act(() => callback(timestamp));
  }

  it('캐시된 KPI 목적지가 즉시 렌더돼도 전환 면을 한 frame 동안 유지한다', async () => {
    const user = userEvent.setup();
    renderWithTheme(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route element={<AppLayout />}>
            <Route
              index
              element={(
                <KpiCard
                  label="관리 고객사"
                  value={12}
                  unit="개사"
                  icon={<BusinessRoundedIcon />}
                  to="/customers"
                />
              )}
            />
            <Route path="customers" element={<div>캐시된 고객사 목록</div>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    );

    await user.click(screen.getByRole('link', { name: '관리 고객사 12개사 보기' }));

    expect(screen.getByText('캐시된 고객사 목록')).toBeInTheDocument();
    const main = screen.getByRole('main');
    const contentFrame = main.parentElement;
    if (!contentFrame) throw new Error('본문 frame 을 찾을 수 없습니다.');
    const transitionOverlay = contentFrame.querySelector<HTMLElement>(
      '[data-route-transition="pending"]',
    );
    expect(main).toHaveAttribute('aria-busy', 'true');
    expect(main).toHaveAttribute('inert');
    expect(transitionOverlay).toBeInTheDocument();
    expect(main).not.toContainElement(transitionOverlay);
    expect(screen.queryByRole('progressbar', { name: '페이지 불러오는 중' }))
      .not.toBeInTheDocument();
    expect(screen.getByRole('status')).toBeEmptyDOMElement();

    runNextFrame(16);
    expect(main).toHaveAttribute('aria-busy', 'true');
    expect(main).toHaveAttribute('inert');
    expect(contentFrame.querySelector('[data-route-transition="pending"]')).toBeInTheDocument();
    expect(screen.queryByRole('progressbar', { name: '페이지 불러오는 중' }))
      .not.toBeInTheDocument();
    expect(screen.getAllByRole('status')).toHaveLength(1);
    expect(screen.getByRole('status')).toBeEmptyDOMElement();

    runNextFrame(32);
    expect(main).not.toHaveAttribute('aria-busy');
    expect(main).not.toHaveAttribute('inert');
    expect(contentFrame.querySelector('[data-route-transition="pending"]'))
      .not.toBeInTheDocument();
    expect(screen.queryByRole('progressbar', { name: '페이지 불러오는 중' }))
      .not.toBeInTheDocument();
    expect(screen.queryByRole('status'))
      .not.toBeInTheDocument();
  });

  it('느린 화면 이동은 숨김 상태를 전달한 뒤 완성 화면을 공개한다', async () => {
    vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] });
    let resolvePage: ((page: { default: () => ReactElement }) => void) | undefined;
    const PendingPage = lazy(() => new Promise<{ default: () => ReactElement }>((resolve) => {
      resolvePage = resolve;
    }));
    renderWithTheme(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route element={<AppLayout />}>
            <Route
              index
              element={(
                <KpiCard
                  label="미확인 결재"
                  value={3}
                  unit="건"
                  icon={<BusinessRoundedIcon />}
                  to="/pending"
                />
              )}
            />
            <Route path="pending" element={<PendingPage />} />
          </Route>
        </Routes>
      </MemoryRouter>,
    );

    fireEvent.click(screen.getByRole('link', { name: '미확인 결재 3건 보기' }));

    const main = screen.getByRole('main');
    const contentFrame = main.parentElement;
    if (!contentFrame) throw new Error('본문 frame 을 찾을 수 없습니다.');
    expect(main).toHaveAttribute('aria-busy', 'true');
    expect(main).toHaveAttribute('inert');
    expect(contentFrame.querySelector('[data-route-transition="pending"]')).toBeInTheDocument();
    expect(screen.queryByRole('progressbar', { name: '페이지 불러오는 중' }))
      .not.toBeInTheDocument();
    const pendingStatuses = screen.getAllByRole('status');
    expect(pendingStatuses).toHaveLength(2);
    pendingStatuses.forEach((status) => expect(status).toBeEmptyDOMElement());

    runNextFrame(16);
    runNextFrame(32);

    expect(main).not.toHaveAttribute('aria-busy');
    expect(main).not.toHaveAttribute('inert');
    expect(contentFrame.querySelector('[data-route-transition="pending"]'))
      .not.toBeInTheDocument();
    expect(screen.getAllByRole('status')).toHaveLength(1);
    expect(screen.getByRole('status')).toBeEmptyDOMElement();

    act(() => vi.advanceTimersByTime(199));
    expect(screen.queryByRole('progressbar', { name: '페이지 불러오는 중' }))
      .not.toBeInTheDocument();
    expect(screen.getByRole('status')).toBeEmptyDOMElement();

    act(() => vi.advanceTimersByTime(1));
    expect(screen.getByRole('status')).toHaveTextContent('페이지 불러오는 중');
    expect(screen.queryByRole('progressbar')).not.toBeInTheDocument();

    if (!resolvePage) throw new Error('지연 화면 요청이 시작되지 않았습니다.');
    await act(async () => {
      resolvePage?.({ default: () => <div>완성된 결재 화면</div> });
      await Promise.resolve();
    });

    expect(screen.getByText('완성된 결재 화면')).toBeInTheDocument();
    expect(screen.queryByRole('progressbar', { name: '페이지 불러오는 중' }))
      .not.toBeInTheDocument();
    expect(screen.queryByRole('status'))
      .not.toBeInTheDocument();
  });
});
