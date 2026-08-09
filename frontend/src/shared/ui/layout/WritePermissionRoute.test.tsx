import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { act, fireEvent, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { renderWithTheme } from '@/test/renderWithTheme';
import WritePermissionRoute from './WritePermissionRoute';

const mocks = vi.hoisted(() => ({
  canRead: true,
  canWrite: false,
  isLoading: false,
  isError: false,
  error: undefined as unknown,
  retry: vi.fn(),
}));

vi.mock('@/shared/hooks/usePermission', () => ({
  usePermissionBoundary: () => mocks,
}));

function RouteHarness() {
  return (
    <MemoryRouter initialEntries={['/customers/new']}>
      <Routes>
        <Route element={<WritePermissionRoute menuCode={MENU_CODE.CUSTOMERS} />}>
          <Route path="/customers/new" element={<div>고객사 등록 폼</div>} />
        </Route>
      </Routes>
    </MemoryRouter>
  );
}

function renderRoute() {
  return renderWithTheme(<RouteHarness />);
}

describe('WritePermissionRoute', () => {
  beforeEach(() => {
    mocks.canRead = true;
    mocks.canWrite = false;
    mocks.isLoading = false;
    mocks.isError = false;
    mocks.error = undefined;
    mocks.retry.mockReset();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('읽기 권한만 있으면 직접 URL 의 등록 폼을 렌더링하지 않는다', () => {
    renderRoute();

    expect(screen.getByText('수정 권한이 없어 이 페이지를 사용할 수 없습니다.'))
      .toBeInTheDocument();
    expect(screen.queryByText('고객사 등록 폼')).not.toBeInTheDocument();
  });

  it('프로필을 불러오는 중에는 권한 없음 화면을 노출하지 않고 허용 후 렌더링한다', () => {
    vi.useFakeTimers();
    mocks.isLoading = true;
    const view = renderRoute();

    expect(screen.queryByRole('progressbar', { name: '페이지 불러오는 중' }))
      .not.toBeInTheDocument();
    expect(screen.queryByText('수정 권한이 없어 이 페이지를 사용할 수 없습니다.'))
      .not.toBeInTheDocument();

    act(() => vi.advanceTimersByTime(200));

    expect(screen.getByRole('progressbar', { name: '페이지 불러오는 중' }))
      .toBeInTheDocument();

    mocks.isLoading = false;
    mocks.canWrite = true;
    view.rerender(<RouteHarness />);

    expect(screen.getByText('고객사 등록 폼')).toBeInTheDocument();
  });

  it('프로필 조회 실패 시 오류와 재시도 경로를 노출한다', () => {
    mocks.isError = true;
    mocks.error = { status: 503, message: '권한 서버 응답 지연' };
    renderRoute();

    expect(screen.getByText('권한 서버 응답 지연')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '다시 시도' }));
    expect(mocks.retry).toHaveBeenCalledOnce();
  });
});
