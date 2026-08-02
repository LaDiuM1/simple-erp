import { beforeEach, describe, expect, it, vi } from 'vitest';
import { renderHook } from '@testing-library/react';
import { expenseScopeFilter } from '@/features/expense/config/expenseListConfig';
import { useExpenseCreatePage } from './useExpenseCreatePage';
import { useExpenseListPage } from './useExpenseListPage';

const mocks = vi.hoisted(() => ({
  navigate: vi.fn(),
  cancel: vi.fn(),
  canWrite: false,
}));

vi.mock('react-router-dom', async (importOriginal) => ({
  ...(await importOriginal<typeof import('react-router-dom')>()),
  useNavigate: () => mocks.navigate,
}));

vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ canRead: true, canWrite: mocks.canWrite }),
}));

vi.mock('@/features/expense/hooks/useExpenseCreateForm', () => ({
  useExpenseCreateForm: () => ({
    handleCancel: mocks.cancel,
    isSaving: false,
  }),
}));

describe('경비 권한 정책', () => {
  beforeEach(() => {
    mocks.canWrite = false;
    vi.clearAllMocks();
  });

  it('읽기 사용자에게 등록 액션을 제공하고 전체 범위 필터는 숨긴다', () => {
    const { result } = renderHook(() => useExpenseListPage());

    expect(result.current.headerActions[0]).toMatchObject({
      design: 'create',
      menuCode: 'EXPENSES',
      permission: 'read',
    });
    expect(result.current.searchFilter).not.toContain(expenseScopeFilter);
  });

  it('쓰기 사용자에게만 전체 범위 필터를 제공한다', () => {
    mocks.canWrite = true;

    const { result } = renderHook(() => useExpenseListPage());

    expect(result.current.searchFilter[0]).toBe(expenseScopeFilter);
  });

  it('경비 등록 화면의 제출 액션도 읽기 권한을 기준으로 한다', () => {
    const { result } = renderHook(() => useExpenseCreatePage());

    expect(result.current.headerActions[1]).toMatchObject({
      design: 'create',
      menuCode: 'EXPENSES',
      permission: 'read',
    });
  });
});
