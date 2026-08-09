import { configureStore } from '@reduxjs/toolkit';
import { describe, expect, it, vi } from 'vitest';
import { http, HttpResponse } from 'msw';
import authReducer from '@/features/auth/store/authSlice';
import demoRuntimeReducer, { setDemoWriteBlocked } from '@/shared/demo/demoRuntimeSlice';
import { snackbarReducer } from '@/shared/ui/feedback/snackbar';
import { server } from '@/test/msw/server';
import { api } from './baseApi';
import { contractApi } from '@/features/contract/api/contractApi';
import { equipmentApi } from '@/features/equipment/api/equipmentApi';
import { approvalApi } from '@/features/approval/api/approvalApi';
import { expenseApi } from '@/features/expense/api/expenseApi';
import { attendanceApi } from '@/features/attendance/api/attendanceApi';
import type {
  ContractCreateRequest,
  ContractStatus,
  ContractUpdateRequest,
} from '@/features/contract/types';
import type { EquipmentSummary } from '@/features/equipment/types';
import type { ExpenseCreateRequest } from '@/features/expense/types';
import type { LeaveCreateRequest } from '@/features/attendance/types';

const emptyPage = {
  content: [],
  page: 0,
  size: 10,
  totalElements: 0,
  totalPages: 0,
  hasNext: false,
};

const linkedEquipment: EquipmentSummary = {
  id: 7,
  customerId: 48,
  customerName: '테스트 고객사',
  contractId: 1,
  contractNo: 'C-001',
  supplierId: 2,
  supplierName: '테스트 공급사',
  productId: 3,
  productModelName: '테스트 모델',
  categoryName: '레이저',
  outputValue: null,
  outputUnit: null,
  serialNo: null,
  installAddress: null,
  installedDate: null,
  oscillatorWarrantyEndDate: null,
  generalWarrantyEndDate: null,
  warrantyInsurance: false,
};

function contractUpdateRequest(status: ContractStatus): ContractUpdateRequest {
  return {
    customerId: 48,
    employeeId: 1,
    productId: 3,
    outputValue: null,
    outputUnit: null,
    optionText: null,
    initialAmount: null,
    finalAmount: 1_000_000,
    cretopGrade: null,
    supportProgramName: null,
    supportProgramStatus: 'NONE',
    contractDate: '2026-08-11',
    dueDate: null,
    orderDate: null,
    expectedArrivalDate: null,
    arrivalDate: null,
    installedDate: null,
    settledDate: null,
    logisticsNote: null,
    status,
  };
}

function response<T>(data: T) {
  return HttpResponse.json({ status: 200, message: 'OK', data });
}

function createTestStore() {
  const store = configureStore({
    reducer: {
      auth: authReducer,
      demoRuntime: demoRuntimeReducer,
      snackbar: snackbarReducer,
      [api.reducerPath]: api.reducer,
    },
    middleware: (getDefaultMiddleware) => getDefaultMiddleware().concat(api.middleware),
  });
  store.dispatch(setDemoWriteBlocked(false));
  return store;
}

describe('workflow cache invalidation', () => {
  it.each(['INSTALLED', 'SETTLED'] satisfies ContractStatus[])(
    '%s 계약 저장 뒤 연결 설비가 보이는 시점에 LIST 캐시를 갱신한다',
    async (status) => {
      let pollReads = 0;
      let materialized = false;
      let transientPollFailure = true;
      server.use(
        http.get('*/api/v1/equipments', ({ request }) => {
          const url = new URL(request.url);
          if (url.searchParams.get('size') === '100') {
            pollReads += 1;
            if (transientPollFailure) {
              transientPollFailure = false;
              return HttpResponse.json(
                { status: 503, message: '일시적인 조회 실패', data: null },
                { status: 503 },
              );
            }
            materialized = pollReads >= 4;
          }
          return response({
            ...emptyPage,
            content: materialized ? [linkedEquipment] : [],
            totalElements: materialized ? 1 : 0,
            totalPages: materialized ? 1 : 0,
          });
        }),
        http.put('*/api/v1/contracts/:id', () => response(null)),
      );
      const store = createTestStore();
      const equipmentArgs = { page: 0, size: 10 };
      const equipmentSubscription = store.dispatch(
        equipmentApi.endpoints.getEquipments.initiate(equipmentArgs),
      );
      await equipmentSubscription.unwrap();

      await store.dispatch(contractApi.endpoints.updateContract.initiate({
        id: 1,
        body: contractUpdateRequest(status),
      })).unwrap();

      // 후속 poll의 timeout/오류는 위 mutation 성공을 뒤집지 않는다. listener 결과가 실제
      // 조회에 나타난 뒤에만 LIST를 무효화해 활성 목록도 최종 상태로 재조회한다.
      await vi.waitFor(() => {
        const cached = equipmentApi.endpoints.getEquipments.select(equipmentArgs)(store.getState());
        expect(cached.data?.content).toEqual([linkedEquipment]);
      }, { timeout: 2_000 });

      expect(pollReads).toBe(4);
      equipmentSubscription.unsubscribe();
    },
  );

  it('신규 설치완료 계약은 응답 contractId로 연결 설비를 확인한다', async () => {
    const contractId = 99;
    const createdEquipment = { ...linkedEquipment, contractId };
    let contractCreated = false;
    server.use(
      http.get('*/api/v1/equipments', () => response({
        ...emptyPage,
        content: contractCreated ? [createdEquipment] : [],
        totalElements: contractCreated ? 1 : 0,
        totalPages: contractCreated ? 1 : 0,
      })),
      http.post('*/api/v1/contracts', () => {
        contractCreated = true;
        return response(contractId);
      }),
    );
    const store = createTestStore();
    const equipmentArgs = { page: 0, size: 10 };
    const equipmentSubscription = store.dispatch(
      equipmentApi.endpoints.getEquipments.initiate(equipmentArgs),
    );
    await equipmentSubscription.unwrap();

    await store.dispatch(contractApi.endpoints.createContract.initiate({
      contractNo: null,
      ...contractUpdateRequest('INSTALLED'),
    } satisfies ContractCreateRequest)).unwrap();

    await vi.waitFor(() => {
      const cached = equipmentApi.endpoints.getEquipments.select(equipmentArgs)(store.getState());
      expect(cached.data?.content).toEqual([createdEquipment]);
    });
    equipmentSubscription.unsubscribe();
  });

  it('경비와 휴가 생성 뒤 결재 LIST 캐시를 각각 폐기한다', async () => {
    let approvalReads = 0;
    server.use(
      http.get('*/api/v1/approvals', () => {
        approvalReads += 1;
        return response(emptyPage);
      }),
      http.post('*/api/v1/expenses', () => response(1)),
      http.post('*/api/v1/leaves', () => response(1)),
    );
    const store = createTestStore();
    const approvalSubscription = store.dispatch(
      approvalApi.endpoints.getApprovals.initiate({ page: 0, size: 10 }),
    );
    await approvalSubscription.unwrap();

    await store.dispatch(expenseApi.endpoints.createExpense.initiate({
      title: '경비',
      items: [],
      approverIds: [2],
    } satisfies ExpenseCreateRequest)).unwrap();
    await vi.waitFor(() => expect(approvalReads).toBe(2));

    await store.dispatch(attendanceApi.endpoints.createLeave.initiate({
      leaveType: 'ANNUAL',
      startDate: '2026-08-11',
      endDate: '2026-08-11',
      reason: null,
      approverIds: [2],
    } satisfies LeaveCreateRequest)).unwrap();
    await vi.waitFor(() => expect(approvalReads).toBe(3));
    approvalSubscription.unsubscribe();
  });

  it('결재 승인 뒤 관리자 휴가와 잔액 LIST 캐시를 모두 폐기한다', async () => {
    let leaveReads = 0;
    let balanceReads = 0;
    server.use(
      http.get('*/api/v1/leaves/balances', () => {
        balanceReads += 1;
        return response([]);
      }),
      http.get('*/api/v1/leaves', () => {
        leaveReads += 1;
        return response(emptyPage);
      }),
      http.post('*/api/v1/approvals/:id/approve', () => response(null)),
    );
    const store = createTestStore();
    const leaveSubscription = store.dispatch(attendanceApi.endpoints.getLeaves.initiate({
      status: null,
      employeeId: null,
      startDate: null,
      endDate: null,
      page: 0,
      size: 10,
    }));
    const balanceSubscription = store.dispatch(
      attendanceApi.endpoints.getLeaveBalances.initiate({ year: 2026 }),
    );
    await Promise.all([leaveSubscription.unwrap(), balanceSubscription.unwrap()]);

    await store.dispatch(approvalApi.endpoints.approveApproval.initiate({
      id: 1,
      body: { comment: null },
    })).unwrap();

    await vi.waitFor(() => {
      expect(leaveReads).toBe(2);
      expect(balanceReads).toBe(2);
    });
    leaveSubscription.unsubscribe();
    balanceSubscription.unsubscribe();
  });
});
