import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderHook } from '@testing-library/react';
import { MENU_CODE } from '@/shared/config/menuConfig';
import { useDashboardPage } from './useDashboardPage';

const mocks = vi.hoisted(() => ({
  navigate: vi.fn(),
  usePermission: vi.fn(),
  useGetDashboardSalesQuery: vi.fn(() => ({ data: undefined })),
  useGetDashboardServiceStatsQuery: vi.fn(() => ({ data: undefined })),
  useGetDashboardSummaryQuery: vi.fn(() => ({ data: undefined })),
  useGetDashboardWarrantyQuery: vi.fn(() => ({ data: undefined })),
}));

vi.mock('react-router-dom', () => ({
  useNavigate: () => mocks.navigate,
}));

vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: mocks.usePermission,
}));

vi.mock('@/shared/hooks/useToday', () => ({
  useToday: () => new Date('2026-08-13T09:00:00+09:00'),
}));

vi.mock('@/features/employee/api/employeeApi', () => ({
  useGetMyProfileQuery: () => ({ data: undefined }),
}));

vi.mock('@/features/dashboard/api/dashboardApi', () => ({
  useGetDashboardSalesQuery: mocks.useGetDashboardSalesQuery,
  useGetDashboardServiceStatsQuery: mocks.useGetDashboardServiceStatsQuery,
  useGetDashboardSummaryQuery: mocks.useGetDashboardSummaryQuery,
  useGetDashboardWarrantyQuery: mocks.useGetDashboardWarrantyQuery,
}));

describe('useDashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.usePermission.mockReturnValue({ canRead: false, canWrite: false });
  });

  it('읽기 권한이 없는 업무 화면의 이동 동작을 제공하지 않는다', () => {
    const { result } = renderHook(() => useDashboardPage());

    expect(result.current.onNavigateCustomers).toBeUndefined();
    expect(result.current.onNavigateCustomer).toBeUndefined();
    expect(result.current.onNavigateSalesContacts).toBeUndefined();
    expect(result.current.onNavigateEmployees).toBeUndefined();
    expect(result.current.onNavigateSalesCustomers).toBeUndefined();
    expect(result.current.onNavigateSalesCustomer).toBeUndefined();
    expect(mocks.useGetDashboardSalesQuery).toHaveBeenCalledWith(undefined, { skip: true });
    expect(mocks.useGetDashboardServiceStatsQuery).toHaveBeenCalledWith(undefined, { skip: true });
    expect(mocks.useGetDashboardWarrantyQuery).toHaveBeenCalledWith(undefined, { skip: true });
  });

  it('읽기 권한이 있는 업무 화면으로만 이동한다', () => {
    mocks.usePermission.mockImplementation((menuCode: string) => ({
      canRead: menuCode === MENU_CODE.CUSTOMERS || menuCode === MENU_CODE.SALES_CUSTOMERS,
      canWrite: false,
    }));

    const { result } = renderHook(() => useDashboardPage());

    result.current.onNavigateCustomers?.();
    result.current.onNavigateCustomer?.(17);
    result.current.onNavigateSalesCustomers?.();
    result.current.onNavigateSalesCustomer?.(21);

    expect(mocks.navigate.mock.calls).toEqual([
      ['/customers'],
      ['/customers/17'],
      ['/sales-customers'],
      ['/sales-customers/21'],
    ]);
    expect(result.current.onNavigateSalesContacts).toBeUndefined();
    expect(result.current.onNavigateEmployees).toBeUndefined();
  });

  it('조회 권한이 있는 보조 쿼리의 오류 상태를 화면 쿼리에 포함한다', () => {
    const salesQuery = {
      data: undefined,
      isLoading: false,
      isError: true,
      error: { status: 503, message: '계약 현황을 불러오지 못했습니다.' },
      refetch: vi.fn(),
    };
    mocks.usePermission.mockImplementation((menuCode: string) => ({
      canRead: menuCode === MENU_CODE.CONTRACTS,
      canWrite: false,
    }));
    mocks.useGetDashboardSalesQuery.mockReturnValueOnce(salesQuery);

    const { result } = renderHook(() => useDashboardPage());

    expect(result.current.queries.sales).toBe(salesQuery);
    expect(result.current.queries.service).toBeUndefined();
    expect(result.current.queries.warranty).toBeUndefined();
  });
});
