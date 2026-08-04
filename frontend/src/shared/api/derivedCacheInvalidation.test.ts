import { configureStore } from '@reduxjs/toolkit';
import { describe, expect, it, vi } from 'vitest';
import { http, HttpResponse } from 'msw';
import authReducer from '@/features/auth/store/authSlice';
import { snackbarReducer } from '@/shared/ui/feedback/snackbar';
import { server } from '@/test/msw/server';
import { api } from './baseApi';
import { afterServiceApi } from '@/features/afterService/api/afterServiceApi';
import { contractApi } from '@/features/contract/api/contractApi';
import { customerApi } from '@/features/customer/api/customerApi';
import { dashboardApi } from '@/features/dashboard/api/dashboardApi';
import { departmentApi } from '@/features/department/api/departmentApi';
import { employeeApi } from '@/features/employee/api/employeeApi';
import { equipmentApi } from '@/features/equipment/api/equipmentApi';
import { positionApi } from '@/features/position/api/positionApi';
import { productApi } from '@/features/product/api/productApi';
import { roleApi } from '@/features/role/api/roleApi';
import { salesContactApi } from '@/features/salesContact/api/salesContactApi';
import { supplierApi } from '@/features/supplier/api/supplierApi';
import type { EngineerRequest } from '@/features/afterService/types';
import type { CustomerUpdateRequest } from '@/features/customer/types';
import type { DepartmentUpdateRequest } from '@/features/department/types';
import type { EmployeeUpdateRequest } from '@/features/employee/types';
import type { EquipmentUpdateRequest } from '@/features/equipment/types';
import type { PositionUpdateRequest } from '@/features/position/types';
import type { ProductUpdateRequest } from '@/features/product/types';
import type { RoleUpdateRequest } from '@/features/role/types';
import type { SalesContactEmploymentUpdateRequest } from '@/features/salesContact/types';
import type { SupplierUpdateRequest } from '@/features/supplier/types';

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

describe('derived cache invalidation', () => {
  it('고객사 이름 변경은 계약·설비·AS 목록과 대시보드 요약을 함께 갱신한다', async () => {
    const reads = { contracts: 0, equipments: 0, services: 0, dashboard: 0 };
    server.use(
      http.get('*/api/v1/contracts', () => { reads.contracts += 1; return response(emptyPage); }),
      http.get('*/api/v1/equipments', () => { reads.equipments += 1; return response(emptyPage); }),
      http.get('*/api/v1/after-services', () => { reads.services += 1; return response(emptyPage); }),
      http.get('*/api/v1/dashboard/summary', () => {
        reads.dashboard += 1;
        return response({ kpi: {} });
      }),
      http.put('*/api/v1/customers/:id', () => response(null)),
    );
    const store = createTestStore();
    const subscriptions = [
      store.dispatch(contractApi.endpoints.getContracts.initiate({ page: 0, size: 20 })),
      store.dispatch(equipmentApi.endpoints.getEquipments.initiate({ page: 0, size: 20 })),
      store.dispatch(afterServiceApi.endpoints.getAfterServices.initiate({ page: 0, size: 20 })),
      store.dispatch(dashboardApi.endpoints.getDashboardSummary.initiate()),
    ];
    await Promise.all(subscriptions.map((subscription) => subscription.unwrap()));

    await store.dispatch(customerApi.endpoints.updateCustomer.initiate({
      id: 1,
      body: {} as CustomerUpdateRequest,
    })).unwrap();

    await vi.waitFor(() => expect(reads).toEqual({
      contracts: 2,
      equipments: 2,
      services: 2,
      dashboard: 2,
    }));
    subscriptions.forEach((subscription) => subscription.unsubscribe());
  });

  it('제품·공급사·직원 이름 변경은 이를 표시하는 파생 목록을 갱신한다', async () => {
    const reads = { contracts: 0, equipments: 0, products: 0, services: 0 };
    server.use(
      http.get('*/api/v1/contracts', () => { reads.contracts += 1; return response(emptyPage); }),
      http.get('*/api/v1/equipments', () => { reads.equipments += 1; return response(emptyPage); }),
      http.get('*/api/v1/products/summary', () => { reads.products += 1; return response(emptyPage); }),
      http.get('*/api/v1/after-services', () => { reads.services += 1; return response(emptyPage); }),
      http.put('*/api/v1/products/:id', () => response(null)),
      http.put('*/api/v1/suppliers/:id', () => response(null)),
      http.put('*/api/v1/employees/:id', () => response(null)),
    );
    const store = createTestStore();
    const subscriptions = [
      store.dispatch(contractApi.endpoints.getContracts.initiate({ page: 0, size: 20 })),
      store.dispatch(equipmentApi.endpoints.getEquipments.initiate({ page: 0, size: 20 })),
      store.dispatch(productApi.endpoints.getProductsSummary.initiate({ page: 0, size: 20 })),
      store.dispatch(afterServiceApi.endpoints.getAfterServices.initiate({ page: 0, size: 20 })),
    ];
    await Promise.all(subscriptions.map((subscription) => subscription.unwrap()));

    await store.dispatch(productApi.endpoints.updateProduct.initiate({
      id: 1,
      body: {} as ProductUpdateRequest,
    })).unwrap();
    await vi.waitFor(() => {
      expect(reads.contracts).toBe(2);
      expect(reads.equipments).toBe(2);
      expect(reads.services).toBe(2);
    });

    await store.dispatch(supplierApi.endpoints.updateSupplier.initiate({
      id: 1,
      body: {} as SupplierUpdateRequest,
    })).unwrap();
    await vi.waitFor(() => {
      expect(reads.contracts).toBe(3);
      expect(reads.equipments).toBe(3);
      expect(reads.products).toBe(3);
      expect(reads.services).toBe(2);
    });

    await store.dispatch(employeeApi.endpoints.updateEmployee.initiate({
      id: 1,
      body: {} as EmployeeUpdateRequest,
    })).unwrap();
    await vi.waitFor(() => {
      expect(reads.contracts).toBe(4);
      expect(reads.services).toBe(3);
    });
    subscriptions.forEach((subscription) => subscription.unsubscribe());
  });

  it('원천 표시값 변경은 영향을 받는 계약·설비·AS 상세만 갱신한다', async () => {
    const reads = { contract: 0, equipment: 0, service: 0 };
    server.use(
      http.get('*/api/v1/contracts/:id', () => {
        reads.contract += 1;
        return response({});
      }),
      http.get('*/api/v1/equipments/:id', () => {
        reads.equipment += 1;
        return response({});
      }),
      http.get('*/api/v1/after-services/:id', () => {
        reads.service += 1;
        return response({});
      }),
      http.put('*/api/v1/customers/:id', () => response(null)),
      http.put('*/api/v1/products/:id', () => response(null)),
      http.put('*/api/v1/suppliers/:id', () => response(null)),
      http.put('*/api/v1/employees/:id', () => response(null)),
      http.put('*/api/v1/equipments/:id', () => response(null)),
      http.put('*/api/v1/after-services/engineers/:id', () => response(null)),
    );
    const store = createTestStore();
    const subscriptions = [
      store.dispatch(contractApi.endpoints.getContract.initiate(11)),
      store.dispatch(equipmentApi.endpoints.getEquipment.initiate(22)),
      store.dispatch(afterServiceApi.endpoints.getAfterService.initiate(33)),
    ];
    await Promise.all(subscriptions.map((subscription) => subscription.unwrap()));

    await store.dispatch(customerApi.endpoints.updateCustomer.initiate({
      id: 1,
      body: {} as CustomerUpdateRequest,
    })).unwrap();
    await vi.waitFor(() => expect(reads).toEqual({ contract: 2, equipment: 2, service: 2 }));

    await store.dispatch(productApi.endpoints.updateProduct.initiate({
      id: 2,
      body: {} as ProductUpdateRequest,
    })).unwrap();
    await vi.waitFor(() => expect(reads).toEqual({ contract: 3, equipment: 3, service: 3 }));

    await store.dispatch(supplierApi.endpoints.updateSupplier.initiate({
      id: 3,
      body: {} as SupplierUpdateRequest,
    })).unwrap();
    await vi.waitFor(() => expect(reads).toEqual({ contract: 4, equipment: 4, service: 3 }));

    await store.dispatch(employeeApi.endpoints.updateEmployee.initiate({
      id: 4,
      body: {} as EmployeeUpdateRequest,
    })).unwrap();
    await vi.waitFor(() => expect(reads).toEqual({ contract: 5, equipment: 4, service: 4 }));

    await store.dispatch(equipmentApi.endpoints.updateEquipment.initiate({
      id: 22,
      body: {} as EquipmentUpdateRequest,
    })).unwrap();
    await vi.waitFor(() => expect(reads).toEqual({ contract: 5, equipment: 5, service: 5 }));

    await store.dispatch(afterServiceApi.endpoints.updateEngineer.initiate({
      id: 5,
      body: {} as EngineerRequest,
    })).unwrap();
    await vi.waitFor(() => expect(reads).toEqual({ contract: 5, equipment: 5, service: 6 }));
    subscriptions.forEach((subscription) => subscription.unsubscribe());
  });

  it('부서·직책·권한 변경은 직원 참조와 같은 세션의 내 프로필을 갱신한다', async () => {
    const reads = { employees: 0, references: 0, contractReferences: 0, profile: 0 };
    server.use(
      http.get('*/api/v1/employees', () => { reads.employees += 1; return response(emptyPage); }),
      http.get('*/api/v1/employees/reference', () => { reads.references += 1; return response(emptyPage); }),
      http.get('*/api/v1/employees/contract-reference', () => {
        reads.contractReferences += 1;
        return response(emptyPage);
      }),
      http.get('*/api/v1/employees/me', () => {
        reads.profile += 1;
        return response({ id: 1, name: '홍길동', menuPermissions: [] });
      }),
      http.put('*/api/v1/departments/:id', () => response(null)),
      http.put('*/api/v1/positions/:id', () => response(null)),
      http.put('*/api/v1/roles/:id', () => response(null)),
    );
    const store = createTestStore();
    const subscriptions = [
      store.dispatch(employeeApi.endpoints.getEmployees.initiate({ page: 0, size: 20 })),
      store.dispatch(employeeApi.endpoints.getEmployeeReferences.initiate({ page: 0, size: 20 })),
      store.dispatch(employeeApi.endpoints.getContractEmployeeReferences.initiate({ page: 0, size: 20 })),
      store.dispatch(employeeApi.endpoints.getMyProfile.initiate()),
    ];
    await Promise.all(subscriptions.map((subscription) => subscription.unwrap()));

    await store.dispatch(departmentApi.endpoints.updateDepartment.initiate({
      id: 1,
      body: {} as DepartmentUpdateRequest,
    })).unwrap();
    await vi.waitFor(() => expect(reads).toEqual({
      employees: 2,
      references: 2,
      contractReferences: 2,
      profile: 2,
    }));

    await store.dispatch(positionApi.endpoints.updatePosition.initiate({
      id: 1,
      body: {} as PositionUpdateRequest,
    })).unwrap();
    await vi.waitFor(() => expect(reads).toEqual({
      employees: 3,
      references: 3,
      contractReferences: 3,
      profile: 3,
    }));

    await store.dispatch(roleApi.endpoints.updateRole.initiate({
      id: 1,
      body: {} as RoleUpdateRequest,
    })).unwrap();
    await vi.waitFor(() => {
      expect(reads.employees).toBe(4);
      expect(reads.profile).toBe(4);
    });
    subscriptions.forEach((subscription) => subscription.unsubscribe());
  });

  it('담당자 재직 정보 변경은 담당자 목록의 현재 소속을 갱신한다', async () => {
    let listReads = 0;
    server.use(
      http.get('*/api/v1/sales-contacts', () => { listReads += 1; return response(emptyPage); }),
      http.put('*/api/v1/sales-contacts/employments/:id', () => response(null)),
    );
    const store = createTestStore();
    const subscription = store.dispatch(
      salesContactApi.endpoints.getSalesContacts.initiate({ page: 0, size: 20 }),
    );
    await subscription.unwrap();

    await store.dispatch(salesContactApi.endpoints.updateSalesContactEmployment.initiate({
      id: 1,
      contactId: 3,
      customerId: 4,
      body: {} as SalesContactEmploymentUpdateRequest,
    })).unwrap();

    await vi.waitFor(() => expect(listReads).toBe(2));
    subscription.unsubscribe();
  });

  it('계약·설비·AS 변경은 대응하는 대시보드 현황만 갱신한다', async () => {
    const reads = { sales: 0, warranty: 0, service: 0 };
    server.use(
      http.get('*/api/v1/dashboard/sales', () => { reads.sales += 1; return response({}); }),
      http.get('*/api/v1/dashboard/warranty', () => { reads.warranty += 1; return response([]); }),
      http.get('*/api/v1/dashboard/service', () => { reads.service += 1; return response({}); }),
      http.delete('*/api/v1/contracts/:id', () => response(null)),
      http.delete('*/api/v1/equipments/:id', () => response(null)),
      http.delete('*/api/v1/after-services/:id', () => response(null)),
    );
    const store = createTestStore();
    const subscriptions = [
      store.dispatch(dashboardApi.endpoints.getDashboardSales.initiate()),
      store.dispatch(dashboardApi.endpoints.getDashboardWarranty.initiate()),
      store.dispatch(dashboardApi.endpoints.getDashboardServiceStats.initiate()),
    ];
    await Promise.all(subscriptions.map((subscription) => subscription.unwrap()));

    await store.dispatch(contractApi.endpoints.deleteContract.initiate(1)).unwrap();
    await vi.waitFor(() => expect(reads).toEqual({ sales: 2, warranty: 1, service: 1 }));

    await store.dispatch(equipmentApi.endpoints.deleteEquipment.initiate(1)).unwrap();
    await vi.waitFor(() => expect(reads).toEqual({ sales: 2, warranty: 2, service: 1 }));

    await store.dispatch(afterServiceApi.endpoints.deleteAfterService.initiate(1)).unwrap();
    await vi.waitFor(() => expect(reads).toEqual({ sales: 2, warranty: 2, service: 2 }));
    subscriptions.forEach((subscription) => subscription.unsubscribe());
  });
});
