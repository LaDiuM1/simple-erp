import { configureStore } from '@reduxjs/toolkit';
import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import authReducer from '@/features/auth/store/authSlice';
import { employeeApi } from '@/features/employee/api/employeeApi';
import { equipmentApi } from '@/features/equipment/api/equipmentApi';
import { snackbarReducer } from '@/shared/ui/feedback/snackbar';
import { server } from '@/test/msw/server';
import { api } from './baseApi';

const emptyPage = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  hasNext: false,
};

function response<T>(data: T) {
  return HttpResponse.json({ status: 200, message: 'OK', data });
}

function createTestStore() {
  return configureStore({
    reducer: {
      auth: authReducer,
      snackbar: snackbarReducer,
      [api.reducerPath]: api.reducer,
    },
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(api.middleware),
  });
}

describe('reference API boundaries', () => {
  it('직원 선택은 관리 목록이 아닌 최소 참조 endpoint를 사용한다', async () => {
    let requestedUrl = '';
    server.use(
      http.get('*/api/v1/employees/reference', ({ request }) => {
        requestedUrl = request.url;
        return response(emptyPage);
      }),
    );
    const store = createTestStore();
    const request = store.dispatch(employeeApi.endpoints.getEmployeeReferences.initiate({
      nameKeyword: '홍',
      status: 'ACTIVE',
      page: 0,
      size: 20,
    }));

    await request.unwrap();

    const url = new URL(requestedUrl);
    expect(url.pathname).toBe('/api/v1/employees/reference');
    expect(url.searchParams.get('nameKeyword')).toBe('홍');
    expect(url.searchParams.get('status')).toBe('ACTIVE');
    request.unsubscribe();
  });

  it('계약자 선택은 계약 데이터 범위 전용 참조 endpoint를 사용한다', async () => {
    let requestedUrl = '';
    server.use(
      http.get('*/api/v1/employees/contract-reference', ({ request }) => {
        requestedUrl = request.url;
        return response(emptyPage);
      }),
    );
    const store = createTestStore();
    const request = store.dispatch(employeeApi.endpoints.getContractEmployeeReferences.initiate({
      status: 'ACTIVE',
      page: 0,
      size: 20,
    }));

    await request.unwrap();

    const url = new URL(requestedUrl);
    expect(url.pathname).toBe('/api/v1/employees/contract-reference');
    expect(url.searchParams.get('status')).toBe('ACTIVE');
    request.unsubscribe();
  });

  it('AS 설비 선택과 보증 조회는 고객사 범위가 포함된 참조 endpoint를 사용한다', async () => {
    const requestedUrls: string[] = [];
    server.use(
      http.get('*/api/v1/equipments/reference', ({ request }) => {
        requestedUrls.push(request.url);
        return response(emptyPage);
      }),
      http.get('*/api/v1/equipments/reference/:id', ({ request, params }) => {
        requestedUrls.push(request.url);
        return response({
          id: Number(params.id),
          customerId: 48,
          productModelName: 'HLA-1530',
          serialNo: null,
          installAddress: null,
          installedDate: null,
          oscillatorWarrantyEndDate: null,
          generalWarrantyEndDate: '2027-03-02',
        });
      }),
    );
    const store = createTestStore();
    const listRequest = store.dispatch(equipmentApi.endpoints.getEquipmentReferences.initiate({
      customerId: 48,
      page: 0,
      size: 20,
    }));
    const detailRequest = store.dispatch(equipmentApi.endpoints.getEquipmentReference.initiate({
      id: 7,
      customerId: 48,
    }));

    await Promise.all([listRequest.unwrap(), detailRequest.unwrap()]);

    const urls = requestedUrls.map((requestedUrl) => new URL(requestedUrl));
    expect(urls.map((url) => url.pathname)).toEqual(expect.arrayContaining([
      '/api/v1/equipments/reference',
      '/api/v1/equipments/reference/7',
    ]));
    expect(urls).toHaveLength(2);
    expect(urls.every((url) => url.searchParams.get('customerId') === '48')).toBe(true);
    listRequest.unsubscribe();
    detailRequest.unsubscribe();
  });
});
