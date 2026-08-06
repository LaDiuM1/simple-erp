import { Suspense } from 'react';
import { MemoryRouter, Outlet } from 'react-router-dom';
import { screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { renderWithTheme } from '@/test/renderWithTheme';
import AppRoutes from './routes';

vi.mock('@/shared/ui/layout/ProtectedRoute', () => ({
  default: () => <Outlet />,
}));

vi.mock('@/shared/hooks/usePermission', () => ({
  usePermission: () => ({ canRead: true, canWrite: false }),
  usePermissionBoundary: () => ({
    canRead: true,
    canWrite: false,
    isLoading: false,
    isError: false,
    error: undefined,
    retry: vi.fn(),
  }),
}));

vi.mock('@/pages/customer/CustomerCreatePage', () => ({
  default: () => <div>고객사 등록 폼</div>,
}));
vi.mock('@/pages/approval/ApprovalCreatePage', () => ({
  default: () => <div>전자결재 작성 폼</div>,
}));
vi.mock('@/pages/expense/ExpenseCreatePage', () => ({
  default: () => <div>경비 작성 폼</div>,
}));
vi.mock('@/pages/attendance/LeaveCreatePage', () => ({
  default: () => <div>휴가 신청 폼</div>,
}));
vi.mock('@/pages/board/BoardCreatePage', () => ({
  default: () => <div>게시글 작성 폼</div>,
}));

function renderPath(path: string) {
  renderWithTheme(
    <MemoryRouter initialEntries={[path]}>
      <Suspense fallback={<div>로딩 중</div>}>
        <AppRoutes />
      </Suspense>
    </MemoryRouter>,
  );
}

describe('AppRoutes 쓰기 권한 경계', () => {
  it('고객사 등록 직접 URL 은 쓰기 권한 경계를 통과해야 한다', () => {
    renderPath('/customers/new');

    expect(screen.getByText('수정 권한이 없어 이 페이지를 사용할 수 없습니다.'))
      .toBeInTheDocument();
    expect(screen.queryByText('고객사 등록 폼')).not.toBeInTheDocument();
  });

  it.each([
    ['/approvals/new', '전자결재 작성 폼'],
    ['/expenses/new', '경비 작성 폼'],
    ['/leaves/new', '휴가 신청 폼'],
    ['/boards/new', '게시글 작성 폼'],
  ])('%s 자기 작성 경로는 메뉴 쓰기 권한과 분리한다', async (path, label) => {
    renderPath(path);

    expect(await screen.findByText(label)).toBeInTheDocument();
    expect(screen.queryByText('수정 권한이 없어 이 페이지를 사용할 수 없습니다.'))
      .not.toBeInTheDocument();
  });
});
