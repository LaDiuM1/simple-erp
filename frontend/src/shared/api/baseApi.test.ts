import { configureStore } from '@reduxjs/toolkit';
import { describe, expect, it, vi } from 'vitest';
import { http, HttpResponse } from 'msw';
import authReducer from '@/features/auth/store/authSlice';
import demoRuntimeReducer, {
  setDemoWriteBlocked,
} from '@/shared/demo/demoRuntimeSlice';
import { snackbarReducer } from '@/shared/ui/feedback/snackbar';
import { server } from '@/test/msw/server';
import { api } from './baseApi';

const probeApi = api.injectEndpoints({
  endpoints: (builder) => ({
    demoWriteGuardProbe: builder.mutation<void, void>({
      query: () => ({ url: '/api/v1/demo-guard-probe', method: 'POST' }),
    }),
    lowercaseWriteGuardProbe: builder.mutation<void, void>({
      query: () => ({ url: '/api/v1/lowercase-demo-guard-probe', method: 'post' }),
    }),
    readOnlyPostProbe: builder.mutation<void, void>({
      query: () => ({ url: '/api/v1/read-only-post-probe', method: 'POST' }),
      extraOptions: { demoWrite: false },
    }),
  }),
});

function createTestStore() {
  return configureStore({
    reducer: {
      auth: authReducer,
      demoRuntime: demoRuntimeReducer,
      snackbar: snackbarReducer,
      [api.reducerPath]: api.reducer,
    },
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(api.middleware),
  });
}

describe('axiosBaseQuery demo write guard', () => {
  it('데모 상태 확인 전에도 mutation을 전송 전에 차단한다', async () => {
    const requestSpy = vi.fn();
    server.use(http.post('*/api/v1/demo-guard-probe', () => {
      requestSpy();
      return HttpResponse.json({ status: 204, message: 'OK', data: null });
    }));
    const testStore = createTestStore();
    const result = await testStore.dispatch(
      probeApi.endpoints.demoWriteGuardProbe.initiate(),
    );

    expect(result).toEqual(expect.objectContaining({
      error: expect.objectContaining({
        status: 503,
        code: 'DEMO_RESET_IN_PROGRESS',
      }),
    }));
    expect(requestSpy).not.toHaveBeenCalled();
  });

  it('소문자 HTTP method도 쓰기 요청으로 정규화해 차단한다', async () => {
    const requestSpy = vi.fn();
    server.use(http.post('*/api/v1/lowercase-demo-guard-probe', () => {
      requestSpy();
      return HttpResponse.json({ status: 204, message: 'OK', data: null });
    }));
    const testStore = createTestStore();

    const result = await testStore.dispatch(
      probeApi.endpoints.lowercaseWriteGuardProbe.initiate(),
    );

    expect(result).toEqual(expect.objectContaining({
      error: expect.objectContaining({ code: 'DEMO_RESET_IN_PROGRESS' }),
    }));
    expect(requestSpy).not.toHaveBeenCalled();
  });

  it('데모 비활성 상태가 확인되면 일반 운영의 mutation을 허용한다', async () => {
    const requestSpy = vi.fn();
    server.use(http.post('*/api/v1/demo-guard-probe', () => {
      requestSpy();
      return HttpResponse.json({ status: 200, message: 'OK', data: null });
    }));
    const testStore = createTestStore();
    testStore.dispatch(setDemoWriteBlocked(false));

    const result = await testStore.dispatch(
      probeApi.endpoints.demoWriteGuardProbe.initiate(),
    );

    expect(result).not.toHaveProperty('error');
    expect(requestSpy).toHaveBeenCalledOnce();
  });

  it('write lock 중에도 의미 메타데이터가 읽기 전용인 POST는 전송한다', async () => {
    const requestSpy = vi.fn();
    server.use(http.post('*/api/v1/read-only-post-probe', () => {
      requestSpy();
      return HttpResponse.json({ status: 200, message: 'OK', data: null });
    }));
    const testStore = createTestStore();

    const result = await testStore.dispatch(
      probeApi.endpoints.readOnlyPostProbe.initiate(),
    );

    expect(result).not.toHaveProperty('error');
    expect(requestSpy).toHaveBeenCalledOnce();
  });
});
