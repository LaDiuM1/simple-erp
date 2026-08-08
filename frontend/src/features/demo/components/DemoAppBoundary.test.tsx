import { beforeEach, describe, expect, it } from 'vitest';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { http, HttpResponse } from 'msw';
import App from '@/app/App';
import { store } from '@/app/store';
import { api } from '@/shared/api/baseApi';
import { logout } from '@/features/auth/store/authSlice';
import { DISABLED_DEMO_STATUS, type DemoStatus } from '@/shared/demo/demoContract';
import { renderWithTheme } from '@/test/renderWithTheme';
import { server } from '@/test/msw/server';
import { apiResponse } from '@/test/msw/handlers';

const enabledStatus: DemoStatus = {
  ...DISABLED_DEMO_STATUS,
  enabled: true,
  environmentName: 'DEMO',
  stateChangedAt: '2099-08-02T06:00:00.000Z',
  generation: 'generation-a',
  lastResetAt: '2099-08-02T06:00:00.000Z',
  nextResetAt: '2099-08-02T12:00:00.000Z',
  notice: '모든 데이터는 합성 데이터이며 주기적으로 초기화됩니다.',
  uploadEnabled: true,
  simulatedLocation: { latitude: 37.5663, longitude: 126.9779 },
  publicAccounts: [{
    label: '관리자',
    description: '전체 데모 흐름',
    loginId: 'demo.manager',
    password: 'public-password',
    recommended: true,
  }],
};

describe('App demo boundary', () => {
  beforeEach(() => {
    store.dispatch(logout());
    store.dispatch(api.util.resetApiState());
    window.localStorage.clear();
    window.history.replaceState({}, '', '/login');
  });

  it('status 제어면 연결 실패 시 로그인 route까지 fail-closed한다', async () => {
    server.use(http.get('*/api/v1/demo/status', () => HttpResponse.error()));

    renderWithTheme(<App />);

    expect(await screen.findByText('환경 상태를 확인하고 있어요')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '로그인' })).not.toBeInTheDocument();
  });

  it('기본 disabled status 응답 성공일 때만 일반 로그인 화면을 통과시킨다', async () => {
    server.use(http.get('*/api/v1/demo/status', () =>
      HttpResponse.json(apiResponse(DISABLED_DEMO_STATUS))));

    renderWithTheme(<App />);

    expect(await screen.findByRole('button', { name: '로그인' })).toBeEnabled();
    expect(screen.queryByLabelText('데모 계정')).not.toBeInTheDocument();
  });

  it('데모 계정 선택은 입력만 채우고 자동 로그인하지 않는다', async () => {
    server.use(http.get('*/api/v1/demo/status', () =>
      HttpResponse.json(apiResponse(enabledStatus))));
    const user = userEvent.setup();

    renderWithTheme(<App />);
    await user.click(await screen.findByRole('button', { name: /관리자.*추천/ }));

    expect(screen.getByLabelText('아이디')).toHaveValue('demo.manager');
    expect(screen.getByLabelText('비밀번호')).toHaveValue('public-password');
    expect(window.location.pathname).toBe('/login');
    expect(store.getState().auth.accessToken).toBeNull();
  });

  it('reset 쓰기 잠금 중에도 로그인 예외와 데모 계정 안내를 유지한다', async () => {
    server.use(http.get('*/api/v1/demo/status', () =>
      HttpResponse.json(apiResponse({ ...enabledStatus, writeLocked: true }))));

    renderWithTheme(<App />);

    expect(await screen.findByRole('button', { name: '로그인' })).toBeEnabled();
    expect(screen.getByLabelText('데모 계정')).toBeInTheDocument();
  });
});
