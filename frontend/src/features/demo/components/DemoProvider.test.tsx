import { type Middleware, configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';
import { MemoryRouter, useLocation } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { screen, waitFor } from '@testing-library/react';
import { http, HttpResponse } from 'msw';
import authReducer, { setToken } from '@/features/auth/store/authSlice';
import demoRuntimeReducer from '@/shared/demo/demoRuntimeSlice';
import { snackbarReducer } from '@/shared/ui/feedback/snackbar';
import { api } from '@/shared/api/baseApi';
import { useDemo } from '@/shared/demo/DemoContext';
import { DISABLED_DEMO_STATUS, type DemoStatus } from '@/shared/demo/demoContract';
import { demoApi } from '@/features/demo/api/demoApi';
import { renderWithTheme } from '@/test/renderWithTheme';
import { apiResponse } from '@/test/msw/handlers';
import { server } from '@/test/msw/server';
import DemoProvider from './DemoProvider';

function Probe() {
  const demo = useDemo();
  const location = useLocation();
  return <div data-testid="probe">{`${demo.status.generation}:${location.pathname}`}</div>;
}

describe('DemoProvider generation coordinator', () => {
  it('generation 변경 시 API 캐시를 비우고 루트로 replace한 뒤 안내를 남긴다', async () => {
    let generation = 'generation-a';
    const actionTypes: string[] = [];
    const recorder: Middleware = () => (next) => (action) => {
      if (typeof action === 'object' && action !== null && 'type' in action) {
        actionTypes.push(String(action.type));
      }
      return next(action);
    };
    const testStore = configureStore({
      reducer: {
        auth: authReducer,
        demoRuntime: demoRuntimeReducer,
        snackbar: snackbarReducer,
        [api.reducerPath]: api.reducer,
      },
      middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware().concat(api.middleware, recorder),
    });
    const status = (): DemoStatus => ({
      ...DISABLED_DEMO_STATUS,
      enabled: true,
      environmentName: 'DEMO',
      state: 'READY',
      stateChangedAt: '2099-08-02T06:00:00.000Z',
      generation,
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
    });
    server.use(http.get('*/api/v1/demo/status', () =>
      HttpResponse.json(apiResponse(status()))));
    testStore.dispatch(setToken('test-token'));

    renderWithTheme(
      <Provider store={testStore}>
        <MemoryRouter initialEntries={['/employees']}>
          <DemoProvider><Probe /></DemoProvider>
        </MemoryRouter>
      </Provider>,
    );
    expect(await screen.findByText('generation-a:/employees')).toBeInTheDocument();
    actionTypes.length = 0;

    generation = 'generation-b';
    void testStore.dispatch(demoApi.endpoints.getDemoStatus.initiate(undefined, {
      forceRefetch: true,
      subscribe: false,
    }));

    await waitFor(() => expect(actionTypes).toContain(`${api.reducerPath}/resetApiState`));
    await waitFor(() => expect(screen.getByTestId('probe')).toHaveTextContent(':/'));
    expect(testStore.getState().snackbar.queue).toEqual(expect.arrayContaining([
      expect.objectContaining({
        severity: 'success',
        message: '데모 데이터가 초기 상태로 복원되었습니다.',
        duration: 8_000,
      }),
    ]));
  });
});
