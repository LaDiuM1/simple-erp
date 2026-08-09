import { configureStore } from '@reduxjs/toolkit';
import { http, HttpResponse } from 'msw';
import { describe, expect, it, vi } from 'vitest';
import authReducer from '@/features/auth/store/authSlice';
import { api } from '@/shared/api/baseApi';
import demoRuntimeReducer from '@/shared/demo/demoRuntimeSlice';
import { snackbarReducer } from '@/shared/ui/feedback/snackbar';
import { server } from '@/test/msw/server';
import { SALES_CUSTOMER_AGGREGATE_CONTRACT_MESSAGE } from '../utils/salesCustomerAggregateContract';
import { salesCustomerApi } from './salesCustomerApi';

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

function response(data: unknown) {
  return HttpResponse.json({ status: 200, message: 'OK', data });
}

describe('sales customer aggregate query', () => {
  it('식별자 계약 위반을 예외 없이 RTK Query 거부 결과로 반환한다', async () => {
    server.use(
      http.get('*/api/v1/sales-customers/aggregates', () => response([
        {
          customerId: 1,
          primaryAssigneeId: null,
          primaryAssigneeName: null,
          activeAssigneeCount: 1,
          activityCount: 2,
          lastActivityDate: null,
        },
      ])),
    );
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const store = createTestStore();
    const request = store.dispatch(
      salesCustomerApi.endpoints.getSalesCustomerAggregates.initiate([1, 2]),
    );

    await expect(request.unwrap()).rejects.toEqual({
      status: 500,
      message: SALES_CUSTOMER_AGGREGATE_CONTRACT_MESSAGE,
    });
    expect((await request).isError).toBe(true);
    expect(consoleError).not.toHaveBeenCalled();
    request.unsubscribe();
    consoleError.mockRestore();
  });

  it('전송 오류도 RTK Query 거부 결과로 보존한다', async () => {
    server.use(
      http.get('*/api/v1/sales-customers/aggregates', () => HttpResponse.json(
        { status: 503, message: '집계 서버 오류', data: null },
        { status: 503 },
      )),
    );
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    const store = createTestStore();
    const request = store.dispatch(
      salesCustomerApi.endpoints.getSalesCustomerAggregates.initiate([1]),
    );

    await expect(request.unwrap()).rejects.toEqual({ status: 503, message: '집계 서버 오류' });
    expect((await request).isError).toBe(true);
    expect(consoleError).not.toHaveBeenCalled();
    request.unsubscribe();
    consoleError.mockRestore();
  });
});
